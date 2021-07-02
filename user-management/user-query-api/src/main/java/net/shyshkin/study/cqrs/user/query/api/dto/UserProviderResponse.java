package net.shyshkin.study.cqrs.user.query.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.user.core.models.Role;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProviderResponse {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private List<Role> roles;
}
