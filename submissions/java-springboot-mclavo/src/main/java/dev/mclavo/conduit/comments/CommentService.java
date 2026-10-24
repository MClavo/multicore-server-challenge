package dev.mclavo.conduit.comments;

import dev.mclavo.conduit.articles.Article;
import dev.mclavo.conduit.articles.ArticleRepository;
import dev.mclavo.conduit.comments.dto.CommentResponses.*;
import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.common.Timestamps;
import dev.mclavo.conduit.profiles.ProfileService;
import dev.mclavo.conduit.users.User;
import dev.mclavo.conduit.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository comments;
    private final ArticleRepository articles;
    private final UserRepository users;
    private final ProfileService profiles;
    public CommentService(CommentRepository comments, ArticleRepository articles, UserRepository users,
            ProfileService profiles) {
        this.comments = comments;
        this.articles = articles;
        this.users = users;
        this.profiles = profiles;
    }

    public Multiple list(String slug, long viewer) {
        Article article = requireArticle(slug);
        return new Multiple(comments.findTop100ByArticleIdOrderByCreatedAtDescIdDesc(article.getId()).stream()
                .map(comment -> new CommentDto(comment.getId(), Timestamps.format(comment.getCreatedAt()),
                        Timestamps.format(comment.getUpdatedAt()), comment.getBody(), profiles.profile(comment.getAuthor(), viewer)))
                .toList());
    }

    @Transactional
    public Single create(String slug, String body, long viewer) {
        Article article = requireArticle(slug);
        User author = users.findById(viewer).orElseThrow(ApiException::unauthorized);
        Comment comment = comments.saveAndFlush(new Comment(article, author, body));
        return new Single(new CommentDto(comment.getId(), Timestamps.format(comment.getCreatedAt()),
                Timestamps.format(comment.getUpdatedAt()), comment.getBody(), profiles.profile(author, false)));
    }

    @Transactional
    public void delete(String slug, long id, long viewer) {
        Comment comment = comments.findForDeletion(id, slug).orElseThrow(() -> ApiException.notFound("Comment"));
        if (comment.getAuthor().getId() != viewer) throw ApiException.forbidden();
        comments.delete(comment);
        comments.flush();
    }

    private Article requireArticle(String slug) {
        return articles.findBySlug(slug).orElseThrow(() -> ApiException.notFound("Article"));
    }
}
