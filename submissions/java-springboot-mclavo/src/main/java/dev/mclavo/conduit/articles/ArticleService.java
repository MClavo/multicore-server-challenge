package dev.mclavo.conduit.articles;

import dev.mclavo.conduit.articles.dto.ArticleRequests.*;
import dev.mclavo.conduit.articles.dto.ArticleResponses.*;
import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.users.User;
import dev.mclavo.conduit.users.UserRepository;
import java.util.List;
import java.util.stream.Stream;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articles;
    private final FavoriteRepository favorites;
    private final UserRepository users;
    private final ArticleViewService views;
    private final ArticleCreationService creation;
    public ArticleService(ArticleRepository articles, FavoriteRepository favorites, UserRepository users,
            ArticleViewService views, ArticleCreationService creation) {
        this.articles = articles;
        this.favorites = favorites;
        this.users = users;
        this.views = views;
        this.creation = creation;
    }

    public Multiple list(String tag, String author, String favorited, Pageable page, long viewer) {
        tag = nonEmpty(tag);
        author = tag == null ? nonEmpty(author) : null;
        favorited = tag == null && author == null ? nonEmpty(favorited) : null;
        return page(tag, author, favorited, null, page, viewer);
    }

    public Multiple feed(Pageable page, long viewer) { return page(null, null, null, viewer, page, viewer); }

    private Multiple page(String tag, String author, String favorited, Long feedViewer, Pageable page, long viewer) {
        List<Article> selected;
        if (page.getOffset() <= Integer.MAX_VALUE) {
            selected = articles.findArticles(tag, author, favorited, feedViewer, page);
        } else {
            // JPA limits setFirstResult to int; keep larger, valid decimal offsets working using scalar IDs.
            List<Long> ids;
            try (Stream<Long> candidates = articles.streamArticleIds(tag, author, favorited, feedViewer)) {
                ids = candidates.skip(page.getOffset()).limit(page.getPageSize()).toList();
            }
            selected = ids.stream().map(id -> articles.findById(id).orElseThrow(() -> ApiException.notFound("Article"))).toList();
        }
        List<Summary> results = selected.stream()
                .map(article -> views.summary(article, viewer)).toList();
        int count = articles.findCountCandidates(tag, author, favorited, feedViewer, PageRequest.of(0, 1000)).size();
        return new Multiple(results, count);
    }

    public Single get(String slug, long viewer) { return new Single(views.detail(require(slug), viewer)); }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Single create(NewArticle request, long viewer) {
        for (int attempt = 0; ; attempt++) {
            long id;
            try { id = creation.create(request, viewer); }
            catch (DataIntegrityViolationException exception) {
                if (attempt >= 4 || !tagConflict(exception)) throw exception;
                continue;
            }
            // Creation has committed before the response query begins.
            return views.getById(id, viewer);
        }
    }

    @Transactional
    public Single update(String slug, UpdateArticle request, long viewer) {
        Article article = articles.findBySlugForUpdate(slug).orElseThrow(() -> ApiException.notFound("Article"));
        requireAuthor(article, viewer);
        article.update(request.title(), request.description(), request.body());
        articles.flush();
        return new Single(views.detail(article, viewer));
    }

    @Transactional
    public void delete(String slug, long viewer) {
        Article article = articles.findBySlugForUpdate(slug).orElseThrow(() -> ApiException.notFound("Article"));
        requireAuthor(article, viewer);
        // PostgreSQL's existing foreign keys cascade favorites, comments and article_tags.
        articles.delete(article);
        articles.flush();
    }

    @Transactional
    public Single favorite(String slug, long viewer, boolean favorite) {
        Article article = require(slug);
        User user = users.findByIdForUpdate(viewer).orElseThrow(ApiException::unauthorized);
        FavoriteId id = new FavoriteId(viewer, article.getId());
        if (favorite) {
            if (!favorites.existsById(id)) favorites.saveAndFlush(new Favorite(user, article));
        } else {
            favorites.remove(id);
        }
        return new Single(views.detail(article, viewer));
    }

    private Article require(String slug) {
        return articles.findBySlug(slug).orElseThrow(() -> ApiException.notFound("Article"));
    }
    private void requireAuthor(Article article, long viewer) {
        if (article.getAuthor().getId() != viewer) throw ApiException.forbidden();
    }
    private static String nonEmpty(String value) { return value == null || value.isEmpty() ? null : value; }
    private static boolean tagConflict(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraint
                    && "tags_name_key".equals(constraint.getConstraintName())) return true;
        }
        return false;
    }
}
