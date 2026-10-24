package dev.mclavo.conduit.articles;

import dev.mclavo.conduit.tags.Tag;
import dev.mclavo.conduit.users.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "articles")
public class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String slug;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @Column(nullable = false, columnDefinition = "text")
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String description;
    @Column(nullable = false, columnDefinition = "text")
    private String body;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @ManyToMany
    @JoinTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @OrderBy("name ASC")
    private Set<Tag> tags = new LinkedHashSet<>();

    protected Article() {}
    public Article(User author, String title, String description, String body, Set<Tag> tags) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.body = body;
        this.tags.addAll(tags);
        // The identity is available after INSERT; the final slug is set in the same transaction.
        this.slug = "pending-" + UUID.randomUUID();
    }
    @PrePersist void onCreate() {
        createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        updatedAt = createdAt;
    }
    public void assignSlug() {
        slug = title.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase(Locale.ROOT)
                .replaceAll("^-|-$", "") + "-" + id;
    }
    public void update(String title, String description, String body) {
        if (title != null) { this.title = title; assignSlug(); }
        if (description != null) this.description = description;
        if (body != null) this.body = body;
        updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public User getAuthor() { return author; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
