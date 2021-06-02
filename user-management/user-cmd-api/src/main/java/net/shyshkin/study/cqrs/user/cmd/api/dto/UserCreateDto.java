package net.shyshkin.study.cqrs.user.cmd.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDto {

    private String firstname;
    private String lastname;
    private String emailAddress;
    private AccountDto account;

}
