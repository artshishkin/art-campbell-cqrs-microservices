package net.shyshkin.study.cqrs.user.query.api.handlers;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.user.core.events.UserRegisteredEvent;
import net.shyshkin.study.cqrs.user.core.events.UserRemovedEvent;
import net.shyshkin.study.cqrs.user.core.events.UserUpdatedEvent;
import net.shyshkin.study.cqrs.user.query.api.exceptions.UserNotFoundException;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ProcessingGroup("user-group")
public class UserEventHandlerImpl implements UserEventHandler {

    private final UserRepository repository;

    @EventHandler
    @Override
    public void on(UserRegisteredEvent event) {
        repository.save(event.getUser());
    }

    @EventHandler
    @Override
    public void on(UserUpdatedEvent event) {
        repository
                .findById(event.getUser().getId())
                .ifPresentOrElse(
                        user -> repository.save(event.getUser()),
                        () -> {
                            throw new UserNotFoundException("User not found by id `" + event.getUser().getId() + "`");
                        }
                );
    }

    @EventHandler
    @Override
    public void on(UserRemovedEvent event) {
        repository
                .findById(event.getId())
                .ifPresentOrElse(
                        repository::delete,
                        () -> {
                            throw new UserNotFoundException("User not found by id `" + event.getId() + "`");
                        }
                );
    }
}
