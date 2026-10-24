package dev.mclavo.conduit.comments.dto;

import dev.mclavo.conduit.profiles.dto.ProfileDto;
import java.util.List;

public final class CommentResponses {
    private CommentResponses() {}
    public record Single(CommentDto comment) {}
    public record Multiple(List<CommentDto> comments) {}
    public record CommentDto(long id, String createdAt, String updatedAt, String body, ProfileDto author) {}
}
