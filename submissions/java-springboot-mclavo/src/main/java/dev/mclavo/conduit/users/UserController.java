package dev.mclavo.conduit.users;

import dev.mclavo.conduit.security.Viewer;
import dev.mclavo.conduit.users.dto.UserRequests.*;
import dev.mclavo.conduit.users.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }

    @PostMapping("/users") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody Registration request) { return users.register(request.user()); }

    @PostMapping("/users/login")
    public UserResponse login(@Valid @RequestBody Login request) { return users.login(request.user()); }

    @GetMapping("/user")
    public UserResponse current(@AuthenticationPrincipal Jwt jwt) {
        return users.current(Viewer.requiredId(jwt), jwt.getTokenValue());
    }

    @PutMapping("/user")
    public UserResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Update request) {
        return users.update(Viewer.requiredId(jwt), jwt.getTokenValue(), request.user());
    }
}
