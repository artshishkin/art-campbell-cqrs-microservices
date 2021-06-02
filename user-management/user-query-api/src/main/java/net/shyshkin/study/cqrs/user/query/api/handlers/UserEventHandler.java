package net.shyshkin.study.cqrs.user.query.api.handlers;

import net.shyshkin.study.cqrs.user.core.events.UserRegisteredEvent;
import net.shyshkin.study.cqrs.user.core.events.UserRemovedEvent;
import net.shyshkin.study.cqrs.user.core.events.UserUpdatedEvent;

public interface UserEventHandler {

    void on(UserRegisteredEvent event);

    void on(UserUpdatedEvent event);

    void on(UserRemovedEvent event);
}
