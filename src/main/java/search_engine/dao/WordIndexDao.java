package search_engine.dao;

import search_engine.entities.Document;
import search_engine.entities.Word;
import search_engine.entities.WordOccurrence;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordIndexDao {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("search_engine");

    // Сохраняет индекс в бд
    public void saveIndex(Map<Path, Map<String, Integer>> perDocIndex) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try (em) {
            // Кэши для быстрого поиска ID по тексту/пути
            Map<String, Long> docIdCache = new HashMap<>();
            Map<String, Long> wordIdCache = new HashMap<>();
            tx.begin();

            // загрузка существующих ID из БД
            List<Object[]> docRows = em.createQuery("SELECT d.filePath, d.id FROM Document d", Object[].class).getResultList();
            docRows.forEach(r -> docIdCache.put((String) r[0], (Long) r[1]));

            List<Object[]> wordRows = em.createQuery("SELECT w.text, w.id FROM Word w", Object[].class).getResultList();
            wordRows.forEach(r -> wordIdCache.put((String) r[0], (Long) r[1]));

            int batchSize = 500;
            int count = 0;

            for (Map.Entry<Path, Map<String, Integer>> docEntry : perDocIndex.entrySet()) {
                Path path = docEntry.getKey();
                String pathStr = path.toAbsolutePath().toString();
                Long docId = docIdCache.get(pathStr);

                // Документа нет - создаем
                if (docId == null) {
                    Document doc = new Document(pathStr, path.getFileName().toString());
                    em.persist(doc);
                    em.flush();
                    docId = doc.getId();
                    docIdCache.put(pathStr, docId);
                } else {
                    // Документ есть - обновляем индекс путем удаления частот слов
                    em.createQuery("DELETE FROM WordOccurrence o WHERE o.document.id = :id")
                            .setParameter("id", docId).executeUpdate();
                }

                // Обработка слов и создание связей слово-документ
                for (Map.Entry<String, Integer> wordEntry : docEntry.getValue().entrySet()) {
                    String wordText = wordEntry.getKey();
                    Long wId = wordIdCache.get(wordText);

                    // Если слова не нашлось в кэше, то создаем его
                    if (wId == null) {
                        Word w = new Word(wordText);
                        em.persist(w);
                        em.flush();
                        wId = w.getId();
                        wordIdCache.put(wordText, wId);
                    }

                    // Вставка объекта WordOccurrence в бд
                    Document refDoc = em.getReference(Document.class, docId);
                    Word refWord = em.getReference(Word.class, wId);
                    em.persist(new WordOccurrence(refDoc, refWord, wordEntry.getValue()));

                    // Пакетная обработка для быстрой загрузки
                    if (++count % batchSize == 0) {
                        // Отправка накопленных Insert'ов
                        em.flush();
                        // Очистка Persistence Context
                        em.clear();
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Ошибка сохранения индекса в БД", e);
        }
    }

    // Поиск документов по слову, возвращает список с частотой по убыванию.
    public List<SearchResult> search(String word) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = """
                    SELECT new search_engine.dao.SearchResult(d.filePath, d.fileName, wo.count)
                    FROM WordOccurrence wo
                    JOIN wo.document d
                    JOIN wo.word w
                    WHERE w.text = :word
                    ORDER BY wo.count DESC
                    """;
            TypedQuery<SearchResult> query = em.createQuery(jpql, SearchResult.class);
            query.setParameter("word", word.toLowerCase());
            return query.getResultList();
        }
    }
}
