package net.shyshkin.study.cqrs.user.cmd.api.mappers;

import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.models.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {

    Account toModel(AccountDto dto);

    AccountDto toDto(Account account);

}
