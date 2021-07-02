package net.shyshkin.study.cqrs.user.query.api.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailPasswordQuery {
    private String email;
    private String password;
}
