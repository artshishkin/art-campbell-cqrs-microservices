package net.shyshkin.study.cqrs.user.query.api.mappers;

import net.shyshkin.study.cqrs.user.core.models.User;
import net.shyshkin.study.cqrs.user.query.api.dto.UserProviderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "username", source = "account.username")
    @Mapping(target = "email", source = "emailAddress")
    @Mapping(target = "roles", source = "account.roles")
    UserProviderResponse toProviderResponse(User user);

}
