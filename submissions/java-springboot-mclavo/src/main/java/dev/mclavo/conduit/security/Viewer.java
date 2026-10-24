package dev.mclavo.conduit.security;

import org.springframework.security.oauth2.jwt.Jwt;
import dev.mclavo.conduit.common.ApiException;

public final class Viewer {
    private Viewer() {}
    public static long id(Jwt jwt) { return jwt == null ? 0 : Long.parseLong(jwt.getSubject()); }
    public static long requiredId(Jwt jwt) {
        if (jwt == null) throw ApiException.unauthorized();
        return id(jwt);
    }
}
