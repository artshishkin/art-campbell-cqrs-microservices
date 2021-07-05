package net.shyshkin.study.cqrs.user.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDto {

    @NotEmpty(message = "{user.firstname.not-empty}")
    private String firstname;

    @NotEmpty(message = "{user.lastname.not-empty}")
    private String lastname;

    @Email(message = "{user.email-address.email}")
    private String emailAddress;

    @NotNull(message = "{user.account.not-empty}")
    @Valid
    private AccountDto account;

}
