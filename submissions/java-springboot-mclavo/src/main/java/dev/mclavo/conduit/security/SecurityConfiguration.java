package dev.mclavo.conduit.security;

import dev.mclavo.conduit.common.ApiError;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); }

    @Bean
    BearerTokenResolver tokenResolver() {
        return request -> {
            String path = request.getServletPath();
            if (("GET".equals(request.getMethod()) && "/api/tags".equals(path))
                    || ("POST".equals(request.getMethod())
                    && ("/api/users".equals(path) || "/api/users/login".equals(path)))) return null;
            var headers = Collections.list(request.getHeaders("Authorization"));
            if (headers.isEmpty()) return null;
            String header = headers.getFirst();
            if (headers.size() != 1 || !header.matches("Token [A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+")) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
            }
            return header.substring(6);
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
            BearerTokenResolver tokenResolver, JsonMapper mapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll()
                        .requestMatchers("/api/user", "/api/articles/feed").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tags", "/api/profiles/*", "/api/articles",
                                "/api/articles/*", "/api/articles/*/comments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/articles", "/api/articles/*/favorite",
                                "/api/articles/*/comments", "/api/profiles/*/follow").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/articles/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/*", "/api/articles/*/favorite",
                                "/api/articles/*/comments/*", "/api/profiles/*/follow").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> writeError(response, mapper, 401))
                        .accessDeniedHandler((request, response, exception) -> writeError(response, mapper, 403)))
                .oauth2ResourceServer(resource -> resource.bearerTokenResolver(tokenResolver)
                        .jwt(jwt -> jwt.decoder(jwtService))
                        .authenticationEntryPoint((request, response, exception) -> writeError(response, mapper, 401))
                        .accessDeniedHandler((request, response, exception) -> writeError(response, mapper, 403)))
                .build();
    }

    private static void writeError(HttpServletResponse response, JsonMapper mapper, int status) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(mapper.writeValueAsBytes(ApiError.of(status == 401
                ? "Invalid or missing authentication" : "Access denied")));
    }
}
