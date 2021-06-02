package net.shyshkin.study.cqrs.user.core.events;

import lombok.Data;

@Data
public class UserRemovedEvent {

    private final String id;

}
