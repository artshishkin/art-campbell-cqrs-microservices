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

    @Size(min = 3, max = 255, message = "{account.username.size}")
    private String username;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
            message = "{account.password.pattern}"
    )
    private String password;

    @NotNull(message = "{account.roles.not-null}")
    @Size(min = 1, message = "{account.roles.size}")
    private List<Role> roles;
}
