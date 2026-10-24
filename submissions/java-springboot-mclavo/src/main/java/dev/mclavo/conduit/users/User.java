package dev.mclavo.conduit.users;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String username;
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String email;
    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;
    @Column(columnDefinition = "text")
    private String bio;
    @Column(columnDefinition = "text")
    private String image;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {}
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS); }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getBio() { return bio; }
    public String getImage() { return image; }

    public void update(String username, String email, String passwordHash, String bio, String image) {
        if (username != null) this.username = username;
        if (email != null) this.email = email;
        if (passwordHash != null) this.passwordHash = passwordHash;
        if (bio != null) this.bio = bio;
        if (image != null) this.image = image;
    }
}
