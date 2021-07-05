package net.shyshkin.study.cqrs.bankaccount.cmd.api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAccountCommand {

    @TargetAggregateIdentifier
    private UUID id;

    @NotEmpty(message = "{account.holder-id.not-empty}")
    @Size(min = 36, max = 36, message = "{account.holder-id.size}")
    private String accountHolderId;

    @NotNull(message = "{account.type.not-null}")
    private AccountType accountType;

    @NotNull(message = "{account.opening-balance.not-null}")
    @PositiveOrZero(message = "{account.opening-balance.positive-or-zero}")
    private BigDecimal openingBalance;

}
