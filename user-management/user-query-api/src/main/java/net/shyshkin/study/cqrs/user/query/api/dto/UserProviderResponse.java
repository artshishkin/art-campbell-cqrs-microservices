package net.shyshkin.study.cqrs.user.query.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.user.core.models.User;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProviderResponse {
    private List<User> users;
}
