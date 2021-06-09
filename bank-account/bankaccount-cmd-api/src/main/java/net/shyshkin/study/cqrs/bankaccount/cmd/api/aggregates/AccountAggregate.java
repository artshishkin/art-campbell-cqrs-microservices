package net.shyshkin.study.cqrs.bankaccount.cmd.api.aggregates;

import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.CloseAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.DepositFundsCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.OpenAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.WithdrawFundsCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.exceptions.NotEnoughBalanceException;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.mappers.AccountMapper;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.services.ApplicationContextProvider;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountClosedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountOpenedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.FundsDepositedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.FundsWithdrawnEvent;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

@Aggregate
@NoArgsConstructor
public class AccountAggregate {

    @AggregateIdentifier
    private UUID id;

    private String accountHolderId;
    private AccountType accountType;
    private BigDecimal balance;

    private AccountMapper mapper;

    @Autowired
    public void setMapper(AccountMapper mapper) {
        this.mapper = mapper;
    }

    @CommandHandler
    public AccountAggregate(OpenAccountCommand command) {

        AccountMapper accountMapper = ApplicationContextProvider.getContext()
                .getBean(AccountMapper.class);
        var accountOpenedEvent = accountMapper.toEvent(command);

        AggregateLifecycle.apply(accountOpenedEvent);

    }

    @EventSourcingHandler
    public void on(AccountOpenedEvent event) {
        this.id = event.getId();
        this.accountHolderId = event.getAccountHolderId();
        this.accountType = event.getAccountType();
        this.balance = event.getOpeningBalance();
    }

    @CommandHandler
    public void handle(DepositFundsCommand command) {

        FundsDepositedEvent fundsDepositedEvent = FundsDepositedEvent.builder()
                .id(command.getId())
                .amount(command.getAmount())
                .balance(balance.add(command.getAmount()))
                .build();

        AggregateLifecycle.apply(fundsDepositedEvent);

    }

    @EventSourcingHandler
    public void on(FundsDepositedEvent event) {
        balance = balance.add(event.getAmount());
    }

    @CommandHandler
    public void handle(WithdrawFundsCommand command) {

        if (balance.compareTo(command.getAmount()) < 0)
            throw new NotEnoughBalanceException(String.format("Withdrawal declined, insufficient funds for account `%s`", id));

        var fundsWithdrawnEvent = FundsWithdrawnEvent.builder()
                .id(command.getId())
                .amount(command.getAmount())
                .balance(balance.subtract(command.getAmount()))
                .build();

        AggregateLifecycle.apply(fundsWithdrawnEvent);

    }

    @EventSourcingHandler
    public void on(FundsWithdrawnEvent event) {
        this.balance = balance.subtract(event.getAmount());
    }

    @CommandHandler
    public void handle(CloseAccountCommand command) {
        var accountClosedEvent = mapper.toEvent(command);
        AggregateLifecycle.apply(accountClosedEvent);
    }

    @EventSourcingHandler
    public void on(AccountClosedEvent event) {
        AggregateLifecycle.markDeleted();
    }

}
