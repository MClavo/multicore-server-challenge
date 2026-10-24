package dev.mclavo.conduit.articles;

import dev.mclavo.conduit.articles.dto.ArticleResponses.*;
import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.common.Timestamps;
import dev.mclavo.conduit.profiles.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds fully materialized DTOs while the persistence context is open. */
@Service
@Transactional(readOnly = true)
public class ArticleViewService {
    private final ArticleRepository articles;
    private final FavoriteRepository favorites;
    private final ProfileService profiles;
    public ArticleViewService(ArticleRepository articles, FavoriteRepository favorites, ProfileService profiles) {
        this.articles = articles;
        this.favorites = favorites;
        this.profiles = profiles;
    }

    public Single getById(long id, long viewer) {
        return new Single(detail(articles.findById(id).orElseThrow(() -> ApiException.notFound("Article")), viewer));
    }

    public Detail detail(Article article, long viewer) {
        return new Detail(article.getSlug(), article.getTitle(), article.getDescription(), article.getBody(),
                articles.findTagNames(article.getId()), Timestamps.format(article.getCreatedAt()),
                Timestamps.format(article.getUpdatedAt()), favorited(article, viewer),
                favorites.countByArticleId(article.getId()), profiles.profile(article.getAuthor(), viewer));
    }

    public Summary summary(Article article, long viewer) {
        return new Summary(article.getSlug(), article.getTitle(), article.getDescription(),
                articles.findTagNames(article.getId()), Timestamps.format(article.getCreatedAt()),
                Timestamps.format(article.getUpdatedAt()), favorited(article, viewer),
                favorites.countByArticleId(article.getId()), profiles.profile(article.getAuthor(), viewer));
    }

    private boolean favorited(Article article, long viewer) {
        return viewer != 0 && favorites.existsById(new FavoriteId(viewer, article.getId()));
    }
}
