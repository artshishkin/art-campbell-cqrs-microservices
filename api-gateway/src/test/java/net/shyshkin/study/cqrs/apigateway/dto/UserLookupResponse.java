package net.shyshkin.study.cqrs.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.user.core.models.User;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLookupResponse {
    private List<User> users;
}
