package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Account;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.commandhandling.gateway.CommandGateway;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Only for manual tests -> Start `infrastructure` and `auth-server` docker-compose files first")
@ActiveProfiles("local")
class UpdateUserControllerManualTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    CommandGateway commandGateway;

    @Autowired
    UserMapper mapper;

    // go to postman and get token for existing user, i.e. username = `shyshkin.art`, password = `P@ssW0rd!`
    String jwtAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpemlDNVhVY1h0X1pSQmdhT0p6OFRSVGpEYm56bGZaTlh3V254UmNNbEkwIn0.eyJleHAiOjE2MjUwNTU0OTQsImlhdCI6MTYyNTA1NTE5NCwianRpIjoiZTJhYjU4MjYtOWM3Mi00OWFjLWI4ZGItOTQ4MWRkYmQ3MzdmIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2F1dGgvcmVhbG1zL2thdGFyaW5hemFydCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlOWQ0YTVlZS00ZDgyLTQ4MDctOGMyMi02MWQ4MGZiNGYyODEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJzcHJpbmdiYW5rQ2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6IjM3YmQ1NzU3LTNmNGQtNGU1My05NjcwLTcxNjI5MWU3ZmJjMiIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1rYXRhcmluYXphcnQiLCJSRUFEX1BSSVZJTEVHRSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJXUklURV9QUklWSUxFR0UiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiQXJ0IFNoeXNoa2luIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2h5c2hraW4uYXJ0IiwiZ2l2ZW5fbmFtZSI6IkFydCIsImZhbWlseV9uYW1lIjoiU2h5c2hraW4iLCJlbWFpbCI6ImQuYXJ0LnNoaXNoa2luQGdtYWlsLmNvbSJ9.pVBG_UkbXm8f5xA5wqRPWwD5FX30BTtzFEYrjb_CgpieVHcFZMWW92GQLNXCCknl6UA1jzvddZOSl-ihww2asXoHT27nhKVMtwFk_6tWYSWRKgVY4nYoAuJSt6DJ2LWqUZQBO96lK5jkxjYrWu7JStXGM41J1iTItrM0ai1SBoW0fqEHLcp-silp2lOayh4kG5uwu-tKXgSGWHD3ArCxita7VNFd1Nqk0g2OZ83xUi10DqbRiTRDg5usZ_kVb3MvhQyRtA-OmUmhmX35dNAd3GXL8UPRbt2QcF0jI7Qb8lRi44o9Bkru32kx-pJrIwe8tQ3fn2U9jgK2kuAO8pFoiQ";

    static User existingUser = null;

    @Test
    void updateUser_valid() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        UserCreateDto dto = mapper.toDto(user);

        //when
        RequestEntity<UserCreateDto> requestEntity = RequestEntity
                .put("/api/v1/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .body(dto);
        ResponseEntity<BaseResponse> responseEntity = restTemplate
                .exchange(requestEntity, BaseResponse.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var baseResponse = responseEntity.getBody();
        assertThat(baseResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("message", "User updated successfully");
    }

    @Nested
    class ValidationFailed {

        @Test
        void firstnameNotValid() {
            //given
            User user = getRandomUser();
            String userId = user.getId();

            UserCreateDto dto = mapper.toDto(user);
            dto.setFirstname(null);

            //when
            RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/api/v1/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .exchange(requestEntity, BaseResponse.class);

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
            User user = getRandomUser();
            String userId = user.getId();

            UserCreateDto dto = mapper.toDto(user);
            dto.getAccount().setPassword("FOO");

            //when
            RequestEntity<UserCreateDto> requestEntity = RequestEntity
                    .put("/api/v1/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .exchange(requestEntity, BaseResponse.class);

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
            User user = getRandomUser();
            String userId = user.getId();

            UserCreateDto dto = mapper.toDto(user);
            dto.getAccount().setRoles(null);

            //when
            var requestEntity = RequestEntity
                    .put("/api/v1/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .exchange(requestEntity, BaseResponse.class);

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
            User user = getRandomUser();
            String userId = user.getId();

            UserCreateDto dto = mapper.toDto(user);
            dto.getAccount().setRoles(Collections.emptyList());

            //when
            var requestEntity = RequestEntity
                    .put("/api/v1/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                    .body(dto);
            ResponseEntity<BaseResponse> responseEntity = restTemplate
                    .exchange(requestEntity, BaseResponse.class);

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

    private User getRandomUser() {

        if (existingUser == null) {
            var newUser = createNewUser();
            var registerUserCommand = RegisterUserCommand.builder()
                    .id(newUser.getId())
                    .user(newUser)
                    .build();
            String aggregateId = commandGateway.sendAndWait(registerUserCommand);
            assertThat(aggregateId).isEqualTo(newUser.getId());
            existingUser = newUser;
        }
        return existingUser;
    }

    private User createNewUser() {
        var account = Account.builder()
                .username(FAKER.name().username())
                .password(FAKER.regexify("[a-z]{6}[1-9]{6}[A-Z]{6}[!@#&()]{2}"))
                .roles(List.of(Role.READ_PRIVILEGE))
                .build();

        return User.builder()
                .id(UUID.randomUUID().toString())
                .firstname(FAKER.name().firstName())
                .lastname(FAKER.name().lastName())
                .emailAddress(FAKER.bothify("????##@gmail.com"))
                .account(account)
                .build();
    }
}