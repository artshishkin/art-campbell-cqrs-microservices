package net.shyshkin.study.cqrs.bankaccount.cmd.api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawFundsCommand {

    @NotNull(message = "id is mandatory")
    @TargetAggregateIdentifier
    private UUID id;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

}
