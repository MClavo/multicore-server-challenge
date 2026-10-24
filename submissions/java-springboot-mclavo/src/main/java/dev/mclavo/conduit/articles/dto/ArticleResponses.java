package dev.mclavo.conduit.articles.dto;

import dev.mclavo.conduit.profiles.dto.ProfileDto;
import java.util.List;

public final class ArticleResponses {
    private ArticleResponses() {}
    public record Single(Detail article) {}
    public record Multiple(List<Summary> articles, int articlesCount) {}
    public record Detail(String slug, String title, String description, String body, List<String> tagList,
            String createdAt, String updatedAt, boolean favorited, long favoritesCount, ProfileDto author) {}
    public record Summary(String slug, String title, String description, List<String> tagList,
            String createdAt, String updatedAt, boolean favorited, long favoritesCount, ProfileDto author) {}
}
