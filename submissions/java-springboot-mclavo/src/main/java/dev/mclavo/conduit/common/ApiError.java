package dev.mclavo.conduit.common;

import java.util.List;

public record ApiError(Errors errors) {
    public record Errors(List<String> body) {}
    public static ApiError of(String message) { return new ApiError(new Errors(List.of(message))); }
}
