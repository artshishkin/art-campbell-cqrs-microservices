package net.shyshkin.study.cqrs.bankaccount.query.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.shyshkin.study.cqrs.bankaccount.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.bankaccount.core.models.BankAccount;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class AccountLookupResponse extends BaseResponse {

    private List<BankAccount> accounts;

    public AccountLookupResponse(String message, List<BankAccount> accounts) {
        super(message);
        this.accounts = accounts;
    }
}
