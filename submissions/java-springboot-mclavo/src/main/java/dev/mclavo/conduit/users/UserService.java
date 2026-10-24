package dev.mclavo.conduit.users;

import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.security.JwtService;
import dev.mclavo.conduit.users.dto.UserRequests.*;
import dev.mclavo.conduit.users.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public UserService(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @Transactional
    public UserResponse register(RegisterUser request) {
        User user = users.saveAndFlush(new User(request.username(), request.email(), passwords.encode(request.password())));
        return response(user, jwt.issue(user.getId()));
    }

    public UserResponse login(LoginUser request) {
        User user = users.findByEmail(request.email()).orElseThrow(ApiException::unauthorized);
        if (!passwords.matches(request.password(), user.getPasswordHash())) throw ApiException.unauthorized();
        return response(user, jwt.issue(user.getId()));
    }

    public UserResponse current(long id, String token) {
        return response(users.findById(id).orElseThrow(ApiException::unauthorized), token);
    }

    @Transactional
    public UserResponse update(long id, String token, UpdateUser request) {
        User user = users.findByIdForUpdate(id).orElseThrow(ApiException::unauthorized);
        user.update(request.username(), request.email(),
                request.password() == null ? null : passwords.encode(request.password()), request.bio(), request.image());
        users.flush();
        return response(user, token);
    }

    private UserResponse response(User user, String token) {
        return new UserResponse(new UserResponse.UserDto(user.getEmail(), token, user.getUsername(), user.getBio(), user.getImage()));
    }
}
