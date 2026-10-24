package dev.mclavo.conduit.comments;

import dev.mclavo.conduit.comments.dto.CommentRequest;
import dev.mclavo.conduit.comments.dto.CommentResponses.*;
import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.security.Viewer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles/{slug}/comments")
public class CommentController {
    private final CommentService comments;
    public CommentController(CommentService comments) { this.comments = comments; }
    @GetMapping
    public Multiple list(@PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
        return comments.list(slug, Viewer.id(jwt));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Single create(@PathVariable String slug, @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return comments.create(slug, request.comment().body(), Viewer.requiredId(jwt));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug, @PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        long commentId;
        try {
            if (!id.matches("-?[0-9]+")) throw new NumberFormatException();
            commentId = Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw ApiException.notFound("Comment");
        }
        comments.delete(slug, commentId, Viewer.requiredId(jwt));
    }
}
