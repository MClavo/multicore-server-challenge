package dev.mclavo.conduit.articles;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FavoriteId implements Serializable {
    @Column(name = "user_id") private Long userId;
    @Column(name = "article_id") private Long articleId;
    protected FavoriteId() {}
    public FavoriteId(long userId, long articleId) { this.userId = userId; this.articleId = articleId; }
    @Override public boolean equals(Object other) {
        return other instanceof FavoriteId that && Objects.equals(userId, that.userId) && Objects.equals(articleId, that.articleId);
    }
    @Override public int hashCode() { return Objects.hash(userId, articleId); }
}
