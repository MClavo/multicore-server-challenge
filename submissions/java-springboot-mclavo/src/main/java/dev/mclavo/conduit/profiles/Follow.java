package dev.mclavo.conduit.profiles;

import dev.mclavo.conduit.users.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "follows")
public class Follow {
    @EmbeddedId
    private FollowId id;
    @MapsId("followerId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id")
    private User follower;
    @MapsId("followeeId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followee_id")
    private User followee;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Follow() {}
    public Follow(User follower, User followee) {
        this.id = new FollowId(follower.getId(), followee.getId());
        this.follower = follower;
        this.followee = followee;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
