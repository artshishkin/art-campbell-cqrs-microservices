package net.shyshkin.study.cqrs.bankaccount.query.api.handlers;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.core.models.BankAccount;
import net.shyshkin.study.cqrs.bankaccount.query.api.dto.AccountLookupResponse;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountByHolderIdQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountByIdQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountWithBalanceQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAllAccountsQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.repositories.AccountRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AccountQueryHandlerImpl implements AccountQueryHandler {

    private final AccountRepository repository;

    @QueryHandler
    @Override
    public AccountLookupResponse findAllAccounts(FindAllAccountsQuery query) {
        List<BankAccount> accounts = repository.findAll();
        String message = accounts.isEmpty() ?
                "Bank Accounts not found" :
                "Successfully returned " + accounts.size() + " Bank Account(s)";
        return new AccountLookupResponse(
                message,
                new ArrayList<>(accounts));
    }

    @QueryHandler
    @Override
    public AccountLookupResponse findAccountById(FindAccountByIdQuery query) {
        UUID id = query.getId();
        var bankAccount = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Bank Account with id `%s` not found", id)));
        return new AccountLookupResponse(
                "Bank Account successfully returned",
                List.of(bankAccount));
    }

    @QueryHandler
    @Override
    public AccountLookupResponse findAccountsByHolderId(FindAccountByHolderIdQuery query) {
        String accountHolderId = query.getAccountHolderId();
        var bankAccountList = repository
                .findByAccountHolderId(accountHolderId);
        String message = bankAccountList.isEmpty() ?
                "No Bank Accounts found" :
                String.format("Successfully returned %d Bank Account(s)", bankAccountList.size());

        return new AccountLookupResponse(
                message,
                new ArrayList<>(bankAccountList));
    }

    @QueryHandler
    @Override
    public AccountLookupResponse findAccountsWithBalance(FindAccountWithBalanceQuery query) {
        List<BankAccount> bankAccounts;
        switch (query.getEqualityType()) {
            case LESS_THEN:
                bankAccounts = repository.findByBalanceLessThan(query.getBalance());
                break;
            case GREATER_THEN:
                bankAccounts = repository.findByBalanceGreaterThan(query.getBalance());
                break;
            default:
                bankAccounts = Collections.emptyList();
        }
        String message = bankAccounts.isEmpty() ?
                "Bank Accounts not found" :
                "Successfully returned " + bankAccounts.size() + " Bank Account(s)";
        return new AccountLookupResponse(message, bankAccounts);
    }
}
