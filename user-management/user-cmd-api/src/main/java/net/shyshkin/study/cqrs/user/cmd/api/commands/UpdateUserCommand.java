package net.shyshkin.study.cqrs.user.cmd.api.commands;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class UpdateUserCommand {

    @TargetAggregateIdentifier
    private final String id;
    private final User user;

}
