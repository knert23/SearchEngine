package search_engine.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class WordIndexer {
    // регулярка разбивает текст на отдельные слова, отсекает пробелы, запятые, табляцию и тд
    private static final Pattern TOKENIZER = Pattern.compile("[^\\p{L}\\p{N}]+");

    public Map<String, Integer> indexFile(Path filePath) throws IOException {
        Map<String, Integer> frequencies = new ConcurrentHashMap<>();

        // Считывание файла и разбиение на слова с помощью регулярки
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = TOKENIZER.split(line);
                for (String rawToken : tokens) {
                    if (rawToken.isEmpty()) continue;
                    // Поулученные слова добавляются в мапу
                    frequencies.merge(rawToken.toLowerCase(), 1, Integer::sum);
                }
            }
        }
        return frequencies;
    }
}