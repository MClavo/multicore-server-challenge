package dev.mclavo.conduit.comments.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(@NotNull @Valid NewComment comment) {
    public record NewComment(@NotEmpty String body) {}
}
