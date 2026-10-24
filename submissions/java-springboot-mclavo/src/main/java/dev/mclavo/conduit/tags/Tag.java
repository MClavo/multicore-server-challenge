package dev.mclavo.conduit.tags;

import jakarta.persistence.*;

@Entity
@Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String name;
    protected Tag() {}
    public Tag(String name) { this.name = name; }
    public Integer getId() { return id; }
    public String getName() { return name; }
}
