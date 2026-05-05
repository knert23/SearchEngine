package search_engine.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ParallelDocumentIndexer {
    private final WordIndexer indexer = new WordIndexer();
    // Мапа: (путь до файла, мапа - (слово в файле, частота))
    private final Map<Path, Map<String, Integer>> perDocIndex = new ConcurrentHashMap<>();
    // Синхронизированный список для добавления путей
    private final List<Path> processedFiles = Collections.synchronizedList(new java.util.ArrayList<>());

    public void processDirectory(Path rootDir, int threadCount) throws IOException {
        if (!Files.isDirectory(rootDir)) {
            throw new IllegalArgumentException("Указанный путь не является директорией: " + rootDir);
        }

        List<Path> files;
        try (Stream<Path> stream = Files.walk(rootDir)) {
            files = stream.filter(Files::isRegularFile).filter(this::isTextFile).toList();
        }

        if (files.isEmpty()) {
            System.out.println("Текстовые файлы не найдены в указанной директории.");
            return;
        }

        System.out.printf("Найдено файлов: %d. Запуск пула на %d потоков%n", files.size(), threadCount);

        List<Callable<Void>> tasks = getCallables(files);

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Индексация завершена.");
        System.out.printf("Проиндексировано файлов: %d%n", processedFiles.size());
    }

    private List<Callable<Void>> getCallables(List<Path> files) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (Path file : files) {
            tasks.add(() -> {
                try {
                    Map<String, Integer> fileMap = indexer.indexFile(file);
                    perDocIndex.put(file, fileMap);
                    processedFiles.add(file);
                } catch (IOException e) {
                    System.err.println("Ошибка индексации файла " + file + ": " + e.getMessage());
                }
                return null;
            });
        }
        return tasks;
    }

    public Map<Path, Map<String, Integer>> getPerDocIndex() {
        return perDocIndex;
    }

    private boolean isTextFile(Path p) {
        return p.toString().toLowerCase().endsWith(".txt");
    }
}
