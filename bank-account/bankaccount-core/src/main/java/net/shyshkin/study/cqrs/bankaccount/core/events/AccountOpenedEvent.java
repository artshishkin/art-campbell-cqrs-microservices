package net.shyshkin.study.cqrs.bankaccount.core.events;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AccountOpenedEvent {

    private final UUID id;
    private final String accountHolderId;
    private final AccountType accountType;
    private final LocalDateTime creationDate;
    private final BigDecimal openingBalance;

}
