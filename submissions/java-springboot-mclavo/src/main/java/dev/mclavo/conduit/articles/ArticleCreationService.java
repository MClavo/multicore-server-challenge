package dev.mclavo.conduit.articles;

import dev.mclavo.conduit.articles.dto.ArticleRequests.NewArticle;
import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.tags.TagService;
import dev.mclavo.conduit.users.User;
import dev.mclavo.conduit.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A separate transaction boundary lets a concurrent tag insertion retry the entire create atomically. */
@Service
public class ArticleCreationService {
    private final ArticleRepository articles;
    private final UserRepository users;
    private final TagService tags;
    public ArticleCreationService(ArticleRepository articles, UserRepository users, TagService tags) {
        this.articles = articles;
        this.users = users;
        this.tags = tags;
    }

    @Transactional
    public long create(NewArticle request, long viewer) {
        User author = users.findById(viewer).orElseThrow(ApiException::unauthorized);
        Article article = articles.saveAndFlush(new Article(author, request.title(), request.description(),
                request.body(), tags.resolve(request.tagList())));
        article.assignSlug();
        articles.flush();
        return article.getId();
    }
}
