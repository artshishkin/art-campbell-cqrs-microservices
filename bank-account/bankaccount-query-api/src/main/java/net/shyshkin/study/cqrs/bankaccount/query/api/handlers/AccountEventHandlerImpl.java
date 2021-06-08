package net.shyshkin.study.cqrs.bankaccount.query.api.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountClosedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountOpenedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.FundsDepositedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.FundsWithdrawnEvent;
import net.shyshkin.study.cqrs.bankaccount.core.models.BankAccount;
import net.shyshkin.study.cqrs.bankaccount.query.api.mappers.AccountMapper;
import net.shyshkin.study.cqrs.bankaccount.query.api.repositories.AccountRepository;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@ProcessingGroup("bank-account-group")
public class AccountEventHandlerImpl implements AccountEventHandler {

    private final AccountRepository repository;
    private final AccountMapper mapper;

    @EventHandler
    @Override
    public void on(AccountOpenedEvent event) {

        BankAccount account = mapper.toEntity(event);
        repository.save(account);

    }

    @EventHandler
    @Override
    public void on(FundsDepositedEvent event) {
        UUID accountId = event.getId();
        BankAccount account = repository
                .findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Bank Account with id `%s` not found", accountId)));
        account.setBalance(event.getBalance());
        repository.save(account);
    }

    @EventHandler
    @Override
    public void on(FundsWithdrawnEvent event) {
        UUID accountId = event.getId();
        BankAccount account = repository
                .findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Bank Account with id `%s` not found", accountId)));
        account.setBalance(event.getBalance());
        repository.save(account);
    }

    @EventHandler
    @Override
    public void on(AccountClosedEvent event) {
        repository.deleteById(event.getId());
    }
}
