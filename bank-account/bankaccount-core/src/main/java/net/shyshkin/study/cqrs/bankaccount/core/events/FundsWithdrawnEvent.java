package net.shyshkin.study.cqrs.bankaccount.core.events;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class FundsWithdrawnEvent {

    private final UUID id;
    private final BigDecimal amount;
    private final BigDecimal balance;

}
