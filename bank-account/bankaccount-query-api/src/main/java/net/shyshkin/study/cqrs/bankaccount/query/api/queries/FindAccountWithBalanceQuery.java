package net.shyshkin.study.cqrs.bankaccount.query.api.queries;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FindAccountWithBalanceQuery {
    private final EqualityType equalityType;
    private final BigDecimal balance;
}
