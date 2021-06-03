package net.shyshkin.study.cqrs.user.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.user.core.models.Role;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    @Size(min = 3, max = 255, message = "username must have at least 3 and at most 255 characters")
    private String username;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
            message = "Password must contain at least one digit" +
                    ", one lowercase Latin character" +
                    ", one uppercase Latin character" +
                    ", one special character like `! @ # & ( )`" +
                    ", must have a length of at least 8 characters and a maximum of 20 characters."
    )
    private String password;

    @NotNull(message = "User role is mandatory")
    @Size(min = 1, message = "User must have at least 1 Role")
    private List<Role> roles;
}
