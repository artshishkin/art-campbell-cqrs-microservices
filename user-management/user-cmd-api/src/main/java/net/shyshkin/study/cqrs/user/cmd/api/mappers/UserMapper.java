package net.shyshkin.study.cqrs.user.cmd.api.mappers;

import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(
        uses = {AccountMapper.class},
        imports = {UUID.class}
)
public interface UserMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    User toModel(UserCreateDto dto);

    UserCreateDto toDto(User user);

}
