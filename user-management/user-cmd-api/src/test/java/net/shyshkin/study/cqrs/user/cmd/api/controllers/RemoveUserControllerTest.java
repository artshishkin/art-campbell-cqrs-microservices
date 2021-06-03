package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commontest.AbstractAxonServerTest;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.models.Account;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RemoveUserControllerTest extends AbstractAxonServerTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    UserMapper mapper;

    static User existingUser = null;

    @Test
    void removeUser_success() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        //when
        ResponseEntity<BaseResponse> responseEntity = restTemplate
                .exchange("/api/v1/users/{id}", HttpMethod.DELETE, null, BaseResponse.class, userId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var baseResponse = responseEntity.getBody();
        assertThat(baseResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("message", "User removed successfully");
    }

    @Test
    void removeUser_absent() {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        ResponseEntity<BaseResponse> responseEntity = restTemplate
                .exchange("/api/v1/users/{id}", HttpMethod.DELETE, null, BaseResponse.class, userId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var baseResponse = responseEntity.getBody();
        assertThat(baseResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("message", "The aggregate was not found in the event store");
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