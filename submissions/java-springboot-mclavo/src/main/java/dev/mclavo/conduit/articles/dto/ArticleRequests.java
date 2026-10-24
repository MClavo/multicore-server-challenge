package dev.mclavo.conduit.articles.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class ArticleRequests {
    private ArticleRequests() {}
    public record Create(@NotNull @Valid NewArticle article) {}
    public record NewArticle(@NotEmpty String title, @NotEmpty String description, @NotEmpty String body,
            List<@NotEmpty String> tagList) {}
    public record Update(@NotNull @Valid UpdateArticle article) {}
    public record UpdateArticle(@Size(min = 1) String title, @Size(min = 1) String description,
            @Size(min = 1) String body) {}
}
