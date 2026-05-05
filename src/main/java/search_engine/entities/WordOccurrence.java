package search_engine.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "word_occurrences")
public class WordOccurrence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(nullable = false)
    private Integer count;

    public WordOccurrence() {}
    public WordOccurrence(Document document, Word word, Integer count) {
        this.document = document;
        this.word = word;
        this.count = count;
    }

    public Long getId() {
        return id;
    }
}
