package net.shyshkin.study.cqrs.apigateway.dto.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAccountCommand {

    private UUID id;
    private String accountHolderId;
    private AccountType accountType;
    private BigDecimal openingBalance;

}

