package search_engine.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path", unique = true, nullable = false)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    public Document() {}
    public Document(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public Long getId() {
        return id;
    }
}
