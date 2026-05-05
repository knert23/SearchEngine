package search_engine.core;

import search_engine.dao.WordIndexDao;
import java.nio.file.Path;

public class Runner {
    static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Укажите путь к директории для индексации файлов");
            return;
        }

        Path rootDir = Path.of(args[0]).toAbsolutePath();
        int threadCount = Runtime.getRuntime().availableProcessors();

        try {
            // Индексация
            ParallelDocumentIndexer processor = new ParallelDocumentIndexer();
            processor.processDirectory(rootDir, threadCount);

            // Сохранение в SQLite
            WordIndexDao dao = new WordIndexDao();
            dao.saveIndex(processor.getPerDocIndex());
            System.out.println("Индекс сохранён в SQLite - search-index.db");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}