package net.shyshkin.study.cqrs.user.core.events;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.user.core.models.User;

@Data
@Builder
public class UserUpdatedEvent {

    private final String id;
    private final User user;

}
