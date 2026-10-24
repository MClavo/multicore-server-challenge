package dev.mclavo.conduit.articles;

import dev.mclavo.conduit.articles.dto.ArticleRequests.*;
import dev.mclavo.conduit.articles.dto.ArticleResponses.*;
import dev.mclavo.conduit.common.OffsetPageRequest;
import dev.mclavo.conduit.security.Viewer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articles;
    public ArticleController(ArticleService articles) { this.articles = articles; }

    @GetMapping
    public Multiple list(@RequestParam(required = false) String tag, @RequestParam(required = false) String author,
            @RequestParam(required = false) String favorited, @RequestParam(required = false) String limit,
            @RequestParam(required = false) String offset, @AuthenticationPrincipal Jwt jwt) {
        return articles.list(tag, author, favorited, OffsetPageRequest.parse(limit, offset), Viewer.id(jwt));
    }
    @GetMapping("/feed")
    public Multiple feed(@RequestParam(required = false) String limit, @RequestParam(required = false) String offset,
            @AuthenticationPrincipal Jwt jwt) {
        return articles.feed(OffsetPageRequest.parse(limit, offset), Viewer.requiredId(jwt));
    }
    @GetMapping("/{slug}")
    public Single get(@PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
        return articles.get(slug, Viewer.id(jwt));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Single create(@Valid @RequestBody Create request, @AuthenticationPrincipal Jwt jwt) {
        return articles.create(request.article(), Viewer.requiredId(jwt));
    }
    @PutMapping("/{slug}")
    public Single update(@PathVariable String slug, @Valid @RequestBody Update request, @AuthenticationPrincipal Jwt jwt) {
        return articles.update(slug, request.article(), Viewer.requiredId(jwt));
    }
    @DeleteMapping("/{slug}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
        articles.delete(slug, Viewer.requiredId(jwt));
    }
    @PostMapping("/{slug}/favorite")
    public Single favorite(@PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
        return articles.favorite(slug, Viewer.requiredId(jwt), true);
    }
    @DeleteMapping("/{slug}/favorite")
    public Single unfavorite(@PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
        return articles.favorite(slug, Viewer.requiredId(jwt), false);
    }
}
