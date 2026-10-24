package dev.mclavo.conduit.profiles;

import dev.mclavo.conduit.common.ApiException;
import dev.mclavo.conduit.profiles.dto.ProfileDto;
import dev.mclavo.conduit.profiles.dto.ProfileResponse;
import dev.mclavo.conduit.users.User;
import dev.mclavo.conduit.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    private final UserRepository users;
    private final FollowRepository follows;
    public ProfileService(UserRepository users, FollowRepository follows) { this.users = users; this.follows = follows; }

    public ProfileResponse get(String username, long viewer) {
        User user = users.findByUsername(username).orElseThrow(() -> ApiException.notFound("Profile"));
        return new ProfileResponse(profile(user, viewer));
    }

    public ProfileDto profile(User user, long viewer) {
        return profile(user, viewer != 0 && follows.existsById(new FollowId(viewer, user.getId())));
    }

    public ProfileDto profile(User user, boolean following) {
        return new ProfileDto(user.getUsername(), user.getBio(), user.getImage(), following);
    }

    @Transactional
    public ProfileResponse follow(String username, long viewer, boolean following) {
        User target = users.findByUsername(username).orElseThrow(() -> ApiException.notFound("Profile"));
        // Serialize this user's relationship writes so simultaneous POSTs stay idempotent.
        User follower = users.findByIdForUpdate(viewer).orElseThrow(ApiException::unauthorized);
        FollowId id = new FollowId(viewer, target.getId());
        if (following) {
            if (!follows.existsById(id)) follows.saveAndFlush(new Follow(follower, target));
        } else {
            follows.remove(id);
        }
        return new ProfileResponse(profile(target, following));
    }
}
