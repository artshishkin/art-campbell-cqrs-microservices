package net.shyshkin.study.cqrs.bankaccount.cmd.api.mappers;

import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.CloseAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.OpenAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountClosedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.events.AccountOpenedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(imports = LocalDateTime.class)
public interface AccountMapper {

    @Mapping(target = "creationDate", expression = "java( LocalDateTime.now() )")
    AccountOpenedEvent toEvent(OpenAccountCommand command);

    AccountClosedEvent toEvent(CloseAccountCommand command);

}
