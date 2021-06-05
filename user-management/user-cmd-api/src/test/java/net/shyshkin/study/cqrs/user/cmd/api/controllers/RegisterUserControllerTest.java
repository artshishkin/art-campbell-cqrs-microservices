package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.cmd.api.dto.RegisterUserResponse;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RegisterUserControllerTest extends AbstractDockerComposeTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    void setUp() {
        if (jwtAccessToken == null)
            getJwtAccessToken("shyshkin.art", "P@ssW0rd!");

        restTemplate = new TestRestTemplate(restTemplateBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .rootUri("http://localhost:" + randomServerPort));
    }

    @Test
    void registerUser_valid() {
        //given
        UserCreateDto dto = createNewUser();

        //when
        ResponseEntity<RegisterUserResponse> responseEntity = restTemplate
                .postForEntity("/api/v1/users", dto, RegisterUserResponse.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("message", "User registered successfully");

        assertThat(registerUserResponse.getId()).isNotNull();

    }

    @Nested
    class ValidationFailed {

        @Test
        void firstnameNotValid() {
            //given
            UserCreateDto dto = createNewUser();
            dto.setFirstname(null);

            //when
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .postForEntity("/api/v1/users", dto, BaseResponse.class);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            var registerUserResponse = responseEntity.getBody();
            assertThat(registerUserResponse).isNotNull();
            assertThat(registerUserResponse.getMessage())
                    .contains("Validation failed for argument ")
                    .contains("on field 'firstname':")
                    .contains("firstname is mandatory");
        }

        @Test
        void passwordNotValid() {
            //given
            UserCreateDto dto = createNewUser();
            dto.getAccount().setPassword("FOO");

            //when
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .postForEntity("/api/v1/users", dto, BaseResponse.class);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            var registerUserResponse = responseEntity.getBody();
            assertThat(registerUserResponse).isNotNull();
            assertThat(registerUserResponse.getMessage())
                    .contains("Validation failed for argument ")
                    .contains("on field 'account.password':")
                    .contains("Password must contain at least one digit, one lowercase Latin");
        }

        @Test
        void rolesNull() {
            //given
            UserCreateDto dto = createNewUser();
            dto.getAccount().setRoles(null);

            //when
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .postForEntity("/api/v1/users", dto, BaseResponse.class);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            var registerUserResponse = responseEntity.getBody();
            assertThat(registerUserResponse).isNotNull();
            assertThat(registerUserResponse.getMessage())
                    .contains("Validation failed for argument ")
                    .contains("on field 'account.roles':")
                    .contains("User role is mandatory");
        }

        @Test
        void rolesEmptyList() {
            //given
            UserCreateDto dto = createNewUser();
            dto.getAccount().setRoles(Collections.emptyList());

            //when
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .postForEntity("/api/v1/users", dto, BaseResponse.class);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            var registerUserResponse = responseEntity.getBody();
            assertThat(registerUserResponse).isNotNull();
            assertThat(registerUserResponse.getMessage())
                    .contains("Validation failed for argument ")
                    .contains("on field 'account.roles':")
                    .contains("User must have at least 1 Role");
        }
    }


    private UserCreateDto createNewUser() {
        var accountDto = AccountDto.builder()
                .username(FAKER.name().username())
                .password(FAKER.regexify("[a-z]{6}[1-9]{6}[A-Z]{6}[!@#&()]{2}"))
                .roles(List.of(Role.READ_PRIVILEGE))
                .build();

        return UserCreateDto.builder()
                .firstname(FAKER.name().firstName())
                .lastname(FAKER.name().lastName())
                .emailAddress(FAKER.bothify("????##@gmail.com"))
                .account(accountDto)
                .build();
    }
}