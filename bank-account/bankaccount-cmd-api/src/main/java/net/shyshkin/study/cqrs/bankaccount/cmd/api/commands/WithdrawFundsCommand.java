package net.shyshkin.study.cqrs.bankaccount.cmd.api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawFundsCommand {

    @TargetAggregateIdentifier
    private UUID id;

    private BigDecimal amount;

}
