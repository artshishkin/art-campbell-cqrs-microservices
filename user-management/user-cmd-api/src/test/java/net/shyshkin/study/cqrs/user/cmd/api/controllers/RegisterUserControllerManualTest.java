package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.dto.RegisterUserResponse;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Role;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Only for manual tests -> Start `docker-compose` first")
class RegisterUserControllerManualTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    TestRestTemplate restTemplate;

    // go to postman and get token for existing user, i.e. username = `shyshkin.art`, password = `P@ssW0rd!`
    String jwtAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MjI4MTY4NDEsInVzZXJfbmFtZSI6InNoeXNoa2luLmFydCIsImF1dGhvcml0aWVzIjpbIldSSVRFX1BSSVZJTEVHRSJdLCJqdGkiOiI5NFZHa2E4YUZxSlFKU25pNlYwSGNBc3RpM1UiLCJjbGllbnRfaWQiOiJzcHJpbmdiYW5rQ2xpZW50Iiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl19.rvTagfG5KNah7wZccifbRA3g-mq8JbtgNx2z3D1eL1w";

    @Test
    void registerUser_valid() {
        //given
        UserCreateDto dto = createNewUser();

        //when
        RequestEntity<UserCreateDto> requestEntity = RequestEntity
                .post("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .body(dto);

        ResponseEntity<RegisterUserResponse> responseEntity = restTemplate
                .exchange(requestEntity, RegisterUserResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity
                    .post("/api/v1/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);

            ResponseEntity<RegisterUserResponse> responseEntity = restTemplate
                    .exchange(requestEntity, RegisterUserResponse.class);

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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity
                    .post("/api/v1/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);

            ResponseEntity<RegisterUserResponse> responseEntity = restTemplate
                    .exchange(requestEntity, RegisterUserResponse.class);

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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity
                    .post("/api/v1/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);

            ResponseEntity<RegisterUserResponse> responseEntity = restTemplate
                    .exchange(requestEntity, RegisterUserResponse.class);

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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity
                    .post("/api/v1/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);

            ResponseEntity<RegisterUserResponse> responseEntity = restTemplate
                    .exchange(requestEntity, RegisterUserResponse.class);

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