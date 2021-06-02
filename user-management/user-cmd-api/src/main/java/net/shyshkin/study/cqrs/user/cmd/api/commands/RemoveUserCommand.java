package net.shyshkin.study.cqrs.user.cmd.api.commands;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
public class RemoveUserCommand {

    @TargetAggregateIdentifier
    private final String id;

}
