package net.shyshkin.study.cqrs.bankaccount.query.api.mappers;

import net.shyshkin.study.cqrs.bankaccount.core.events.AccountOpenedEvent;
import net.shyshkin.study.cqrs.bankaccount.core.models.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountMapper {

    @Mapping(target = "balance", source = "openingBalance")
    BankAccount toEntity(AccountOpenedEvent event);

}
