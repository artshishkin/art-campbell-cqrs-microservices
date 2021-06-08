package net.shyshkin.study.cqrs.bankaccount.query.api.handlers;

import net.shyshkin.study.cqrs.bankaccount.core.events.AccountClosedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountOpenedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.FundsDepositedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.FundsWithdrawnEvent;

public interface AccountEventHandler {

    void on(AccountOpenedEvent event);

    void on(FundsDepositedEvent event);

    void on(FundsWithdrawnEvent event);

    void on(AccountClosedEvent event);

}
