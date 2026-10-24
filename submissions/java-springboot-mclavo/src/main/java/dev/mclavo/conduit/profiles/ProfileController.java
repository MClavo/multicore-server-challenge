package dev.mclavo.conduit.profiles;

import dev.mclavo.conduit.profiles.dto.ProfileResponse;
import dev.mclavo.conduit.security.Viewer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/{username}")
public class ProfileController {
    private final ProfileService profiles;
    public ProfileController(ProfileService profiles) { this.profiles = profiles; }
    @GetMapping
    public ProfileResponse get(@PathVariable String username, @AuthenticationPrincipal Jwt jwt) {
        return profiles.get(username, Viewer.id(jwt));
    }
    @PostMapping("/follow")
    public ProfileResponse follow(@PathVariable String username, @AuthenticationPrincipal Jwt jwt) {
        return profiles.follow(username, Viewer.requiredId(jwt), true);
    }
    @DeleteMapping("/follow")
    public ProfileResponse unfollow(@PathVariable String username, @AuthenticationPrincipal Jwt jwt) {
        return profiles.follow(username, Viewer.requiredId(jwt), false);
    }
}
