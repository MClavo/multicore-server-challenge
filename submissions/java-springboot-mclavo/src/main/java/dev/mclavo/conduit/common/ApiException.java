package dev.mclavo.conduit.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
    public static ApiException unauthorized() { return new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"); }
    public static ApiException forbidden() { return new ApiException(HttpStatus.FORBIDDEN, "Only the author may do this"); }
    public static ApiException notFound(String resource) { return new ApiException(HttpStatus.NOT_FOUND, resource + " not found"); }
    public static ApiException invalid(String message) { return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, message); }
}
