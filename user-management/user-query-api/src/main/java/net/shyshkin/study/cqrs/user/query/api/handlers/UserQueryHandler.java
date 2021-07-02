package net.shyshkin.study.cqrs.user.query.api.handlers;

import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.UserProviderResponse;
import net.shyshkin.study.cqrs.user.query.api.queries.FindAllUsersQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByEmailQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByIdQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.SearchUsersQuery;

public interface UserQueryHandler {

    UserLookupResponse getAllUsers(FindAllUsersQuery query);

    UserLookupResponse getUserById(FindUserByIdQuery query);

    UserProviderResponse getUserByEmail(FindUserByEmailQuery query);

    UserLookupResponse searchUsers(SearchUsersQuery query);

}
