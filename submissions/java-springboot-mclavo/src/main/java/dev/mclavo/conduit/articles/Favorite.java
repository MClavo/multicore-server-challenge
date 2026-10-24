package dev.mclavo.conduit.articles;

import dev.mclavo.conduit.users.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "favorites")
public class Favorite {
    @EmbeddedId private FavoriteId id;
    @MapsId("userId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id") private User user;
    @MapsId("articleId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id") private Article article;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected Favorite() {}
    public Favorite(User user, Article article) {
        this.id = new FavoriteId(user.getId(), article.getId());
        this.user = user;
        this.article = article;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
