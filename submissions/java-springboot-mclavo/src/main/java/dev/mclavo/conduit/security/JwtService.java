package dev.mclavo.conduit.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** HS256 with the benchmark's literal secret, which can be shorter than Nimbus's minimum key size. */
@Service
public class JwtService implements JwtDecoder {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final long TOKEN_LIFETIME = 30L * 24 * 60 * 60;
    private final SecretKeySpec key;
    private final JsonMapper mapper;

    public JwtService(@Value("${challenge.jwt-secret}") String secret, JsonMapper mapper) {
        if (secret.isEmpty()) throw new IllegalArgumentException("JWT_SECRET must not be empty");
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.mapper = mapper;
    }

    public String issue(long userId) {
        long now = Instant.now().getEpochSecond();
        String header = encode(mapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
        String claims = encode(mapper.writeValueAsBytes(Map.of(
                "sub", Long.toString(userId), "iat", now, "exp", now + TOKEN_LIFETIME)));
        String message = header + "." + claims;
        return message + "." + encode(sign(message));
    }

    @Override
    public Jwt decode(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) throw new IllegalArgumentException();
            JsonNode header = mapper.readTree(DECODER.decode(parts[0]));
            if (!header.isObject() || !"HS256".equals(header.path("alg").asString())
                    || header.has("crit") || header.has("b64")) throw new IllegalArgumentException();
            if (!MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), DECODER.decode(parts[2]))) {
                throw new IllegalArgumentException();
            }
            JsonNode claims = mapper.readTree(DECODER.decode(parts[1]));
            JsonNode subject = claims.path("sub");
            JsonNode expiration = claims.path("exp");
            if (!subject.isString() || !subject.asString().matches("[0-9]+")
                    || Long.parseLong(subject.asString()) <= 0
                    || !expiration.isIntegralNumber() || !expiration.canConvertToLong()) {
                throw new IllegalArgumentException();
            }
            Instant expiresAt = Instant.ofEpochSecond(expiration.asLong());
            if (!expiresAt.isAfter(Instant.now())) throw new IllegalArgumentException();
            JsonNode notBefore = claims.path("nbf");
            if (!notBefore.isMissingNode() && (!notBefore.isIntegralNumber() || !notBefore.canConvertToLong()
                    || Instant.ofEpochSecond(notBefore.asLong()).isAfter(Instant.now()))) {
                throw new IllegalArgumentException();
            }
            JsonNode issued = claims.path("iat");
            Instant issuedAt = issued.isIntegralNumber() && issued.canConvertToLong()
                    ? Instant.ofEpochSecond(issued.asLong()) : null;
            return Jwt.withTokenValue(token).header("alg", "HS256")
                    .subject(subject.asString()).issuedAt(issuedAt).expiresAt(expiresAt).build();
        } catch (RuntimeException exception) {
            throw new BadJwtException("Invalid or expired token", exception);
        }
    }

    private byte[] sign(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(message.getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HS256 is unavailable", exception);
        }
    }

    private static String encode(byte[] value) { return ENCODER.encodeToString(value); }
}
