package net.shyshkin.study.cqrs.bankaccount.cmd.api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAccountCommand {

    @TargetAggregateIdentifier
    private UUID id;

    private String accountHolderId;

    private AccountType accountType;

    private BigDecimal openingBalance;

}
