package search_engine.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "words")
public class Word {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_text", unique = true, nullable = false)
    private String text;

    public Word() {}
    public Word(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }
}