package search_engine.dao;

// DTO для передачи данных из DAO на веб.
public record SearchResult(String filePath, String fileName, int count) {}
