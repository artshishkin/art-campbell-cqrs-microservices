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

    @NotEmpty(message = "Account Holder Id is MANDATORY")
    @Size(min = 36, max = 36, message = "Must be UUID and have 36 characters long")
    private String accountHolderId;

    @NotNull(message = "Account type is MANDATORY")
    private AccountType accountType;

    @NotNull(message = "Opening balance must not be null or empty")
    @PositiveOrZero(message = "Opening balance must not be negative")
    private BigDecimal openingBalance;

}
