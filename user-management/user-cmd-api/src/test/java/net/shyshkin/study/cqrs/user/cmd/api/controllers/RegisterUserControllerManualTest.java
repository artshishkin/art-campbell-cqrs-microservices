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
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Only for manual tests -> Start `infrastructure` and `auth-server` docker-compose files first")
@ActiveProfiles("local")
class RegisterUserControllerManualTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    TestRestTemplate restTemplate;

    // go to postman and get token for existing user, i.e. username = `shyshkin.art`, password = `P@ssW0rd!`
    String jwtAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpemlDNVhVY1h0X1pSQmdhT0p6OFRSVGpEYm56bGZaTlh3V254UmNNbEkwIn0.eyJleHAiOjE2MjUwNTI0ODUsImlhdCI6MTYyNTA1MjE4NSwianRpIjoiNmY1NDIyYzQtOTY2OS00NDVjLWIyNjEtNTVhMDBkNzk1NTliIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2F1dGgvcmVhbG1zL2thdGFyaW5hemFydCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlOWQ0YTVlZS00ZDgyLTQ4MDctOGMyMi02MWQ4MGZiNGYyODEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJzcHJpbmdiYW5rQ2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6IjUxYTBkM2ZkLTU1MGUtNDBlMC1hMjUyLWY3MGQ5NDZhMjVhYiIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1rYXRhcmluYXphcnQiLCJSRUFEX1BSSVZJTEVHRSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJXUklURV9QUklWSUxFR0UiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiQXJ0IFNoeXNoa2luIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2h5c2hraW4uYXJ0IiwiZ2l2ZW5fbmFtZSI6IkFydCIsImZhbWlseV9uYW1lIjoiU2h5c2hraW4iLCJlbWFpbCI6ImQuYXJ0LnNoaXNoa2luQGdtYWlsLmNvbSJ9.b4NeiXSpIqyysEXBvo4AunZCtX66LxmIOAwldg7RiKg5WIuGKWUmAyRQou7-LwOcWy58ZoDlD5vqwTZqBiXHWNZ4UEfdetmQoTPLYAgWLvSyfXi9UiReFkaCvrMQylLtjZbVnREySJ6ONXQGv-hc5W_HxvJTVGavbdbZk8unoWpEjtqPTK41Ib-Hg5vBYP3L-qV9at8akINGASAQnOjG0x7bISA1NCrJH-0RsWtLHp66B5II9r9-9hzoWRb4psCyaU-_mSHjK5LmYnJ_S3xnz6n5MbjgSj_rTyAawqH0QP4InzFljV8Nj8uHLarGlltxhaYMtbxkvW1kRW12iFpv2Q";

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