package net.shyshkin.study.cqrs.bankaccount.cmd.api.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloseAccountCommand {

    @TargetAggregateIdentifier
    private UUID id;

}
