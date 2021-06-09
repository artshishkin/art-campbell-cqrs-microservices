package net.shyshkin.study.cqrs.bankaccount.query.api.handlers;


import net.shyshkin.study.cqrs.bankaccount.query.api.dto.AccountLookupResponse;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountByHolderIdQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountByIdQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAccountWithBalanceQuery;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.FindAllAccountsQuery;

public interface AccountQueryHandler {

    AccountLookupResponse findAllAccounts(FindAllAccountsQuery query);

    AccountLookupResponse findAccountById(FindAccountByIdQuery query);

    AccountLookupResponse findAccountsByHolderId(FindAccountByHolderIdQuery query);

    AccountLookupResponse findAccountsWithBalance(FindAccountWithBalanceQuery query);

}
