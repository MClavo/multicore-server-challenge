package dev.mclavo.conduit.users.dto;

import dev.mclavo.conduit.common.BcryptPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UserRequests {
    private UserRequests() {}
    public record Registration(@NotNull @Valid RegisterUser user) {}
    public record RegisterUser(@NotEmpty String username, @NotEmpty String email,
            @NotEmpty @BcryptPassword String password) {}
    public record Login(@NotNull @Valid LoginUser user) {}
    public record LoginUser(@NotEmpty String email, @NotEmpty @BcryptPassword String password) {}
    public record Update(@NotNull @Valid UpdateUser user) {}
    public record UpdateUser(@Size(min = 1) String email, @Size(min = 1) String username,
            @Size(min = 1) @BcryptPassword String password, String bio, String image) {}
}
