package net.shyshkin.study.cqrs.user.storage.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String userId;
    private List<String> roles;
}