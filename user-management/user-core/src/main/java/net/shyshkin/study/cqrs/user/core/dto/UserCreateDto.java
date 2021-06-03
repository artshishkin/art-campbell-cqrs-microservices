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

    @NotEmpty(message = "firstname is mandatory")
    private String firstname;

    @NotEmpty(message = "lastname is mandatory")
    private String lastname;

    @Email(message = "please provide a valid email address")
    private String emailAddress;

    @NotNull(message = "please provide account credentials")
    @Valid
    private AccountDto account;

}
