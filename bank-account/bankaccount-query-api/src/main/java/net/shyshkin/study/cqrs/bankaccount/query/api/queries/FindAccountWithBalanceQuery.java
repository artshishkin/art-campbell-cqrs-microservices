package net.shyshkin.study.cqrs.bankaccount.query.api.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindAccountWithBalanceQuery {
    private EqualityType equalityType;
    private BigDecimal balance;
}
