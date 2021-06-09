package net.shyshkin.study.cqrs.bankaccount.query.api.controllers;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.query.api.dto.AccountLookupResponse;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountByHolderIdQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountByIdQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountWithBalanceQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAllAccountsQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('READ_PRIVILEGE')")
public class AccountLookupController {

    private final QueryGateway queryGateway;

    @GetMapping
    public AccountLookupResponse findAllAccounts() {
        var query = new FindAllAccountsQuery();
        return queryGateway.query(query, AccountLookupResponse.class).join();
    }

    @GetMapping("/{id}")
    public AccountLookupResponse findAccountById(@PathVariable UUID id) {
        var query = new FindAccountByIdQuery(id);
        return queryGateway.query(query, AccountLookupResponse.class).join();
    }

    @GetMapping(params = "accountHolderId")
    public AccountLookupResponse findAccountsByHolderId(@RequestParam String accountHolderId) {
        var query = new FindAccountByHolderIdQuery(accountHolderId);
        return queryGateway.query(query, AccountLookupResponse.class).join();
    }

    @GetMapping(params = {"equalityType", "balance"})
    public AccountLookupResponse findAccountsWithBalance(FindAccountWithBalanceQuery query) {
        return queryGateway.query(query, AccountLookupResponse.class).join();
    }
}
