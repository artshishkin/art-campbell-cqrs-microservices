package net.shyshkin.study.cqrs.user.query.api.handlers;

import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.UserProviderResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.VerificationPasswordResponse;
import net.shyshkin.study.cqrs.user.query.api.queries.*;

public interface UserQueryHandler {

    UserLookupResponse getAllUsers(FindAllUsersQuery query);

    UserLookupResponse getUserById(FindUserByIdQuery query);

    UserProviderResponse getUserByEmail(FindUserByEmailQuery query);

    VerificationPasswordResponse verifyEmailAndPassword(VerifyEmailPasswordQuery query);

    UserProviderResponse getUserByUsername(FindUserByUsernameQuery query);

    UserLookupResponse searchUsers(SearchUsersQuery query);

}
