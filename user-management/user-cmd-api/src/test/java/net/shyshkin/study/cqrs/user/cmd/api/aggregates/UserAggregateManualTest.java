package net.shyshkin.study.cqrs.user.cmd.api.aggregates;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RemoveUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commands.UpdateUserCommand;
import net.shyshkin.study.cqrs.user.core.models.Account;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Disabled("Only for manual tests -> Start `docker-compose` first")
class UserAggregateManualTest {

    @Autowired
    CommandGateway commandGateway;

    public static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Test
    void registerUser() {
        //given
        User user = createNewUser();

        //when
        var command = RegisterUserCommand.builder()
                .id(user.getId())
                .user(user)
                .build();
        String aggregateId = commandGateway.sendAndWait(command);

        //then
        assertThat(aggregateId)
                .isEqualTo(command.getId());
        log.debug("Aggregate ID: `{}` for command {}", aggregateId, command);
        log.debug("View in Axon console at localhost:8024 result of command - event should be invoked");
    }

    @Test
    void updateUser() {
        //given
        User user = registerNewUser();
        user.setFirstname(user.getFirstname().toUpperCase());
        user.getAccount().setRoles(List.of(Role.WRITE_PRIVILEGE));

        //when
        var command = UpdateUserCommand
                .builder()
                .id(user.getId())
                .user(user)
                .build();
        Object result = commandGateway.sendAndWait(command);

        //then
        log.debug("Result: `{}` for command {}", result, command);
        log.debug("View in Axon console at localhost:8024 result of command - events should be invoked");
    }

    @Test
    void removeUser() {
        //given
        User user = registerNewUser();

        var command = new RemoveUserCommand(user.getId());

        //when
        Object result = commandGateway.sendAndWait(command);

        //then
        log.debug("Result: `{}` for command {}", result, command);
        log.debug("View in Axon console at localhost:8024 result of command - events should be invoked");
    }

    private User registerNewUser() {
        User user = createNewUser();

        var command = RegisterUserCommand.builder()
                .id(user.getId())
                .user(user)
                .build();

        commandGateway.sendAndWait(command);
        return user;
    }

    private User createNewUser() {
        var account = Account.builder()
                .username(FAKER.name().username())
                .password(FAKER.regexify("[a-z1-9]{10}"))
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