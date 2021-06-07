package net.shyshkin.study.cqrs.user.cmd.api.aggregates;

import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RemoveUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commands.UpdateUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.config.ApplicationContextProvider;
import net.shyshkin.study.cqrs.user.cmd.api.config.PasswordEncoder;
import net.shyshkin.study.cqrs.user.core.events.UserRegisteredEvent;
import net.shyshkin.study.cqrs.user.core.events.UserRemovedEvent;
import net.shyshkin.study.cqrs.user.core.events.UserUpdatedEvent;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Aggregate
@NoArgsConstructor
public class UserAggregate {

    @AggregateIdentifier
    private String id;
    private User user;

    private PasswordEncoder passwordEncoder;

    @CommandHandler
    public UserAggregate(RegisterUserCommand command) {

        var newUser = command.getUser();
        newUser.setId(command.getId());
        var password = newUser.getAccount().getPassword();
        var passwordEncoder = ApplicationContextProvider.getContext().getBean(PasswordEncoder.class);
        var hashedPassword = passwordEncoder.hashPassword(password);
        newUser.getAccount().setPassword(hashedPassword);

        var event = UserRegisteredEvent.builder()
                .id(command.getId())
                .user(newUser)
                .build();

        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handle(UpdateUserCommand command) {

        var updatedUser = command.getUser();
        updatedUser.setId(command.getId());

        String password = updatedUser.getAccount().getPassword();
        String hashedPassword = passwordEncoder.hashPassword(password);
        updatedUser.getAccount().setPassword(hashedPassword);

        var event = UserUpdatedEvent.builder()
                .id(UUID.randomUUID().toString())
                .user(updatedUser)
                .build();

        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handle(RemoveUserCommand command) {
        var event = new UserRemovedEvent(command.getId());

        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(UserRegisteredEvent event) {
        this.id = event.getId();
        this.user = event.getUser();
    }

    @EventSourcingHandler
    public void on(UserUpdatedEvent event) {
        this.user = event.getUser();
    }

    @EventSourcingHandler
    public void on(UserRemovedEvent event) {
        AggregateLifecycle.markDeleted();
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
