package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.cmd.api.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Only for manual tests -> Start `docker-compose` first")
class UpdateUserControllerManualTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    CommandGateway commandGateway;

    @Autowired
    UserMapper mapper;

    static User existingUser = null;

    @Test
    void updateUser_valid() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        UserCreateDto dto = mapper.toDto(user);

        //when
        RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/api/v1/users/{id}", userId).body(dto);
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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/api/v1/users/{id}", userId).body(dto);
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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/api/v1/users/{id}", userId).body(dto);
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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/api/v1/users/{id}", userId).body(dto);
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
            RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/api/v1/users/{id}", userId).body(dto);
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