package net.shyshkin.study.cqrs.user.query.api.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.models.User;
import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.exceptions.UserNotFoundException;
import net.shyshkin.study.cqrs.user.query.api.queries.FindAllUsersQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByIdQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.SearchUsersQuery;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryHandlerImpl implements UserQueryHandler {

    private final UserRepository repository;

    @QueryHandler
    @Override
    public UserLookupResponse getAllUsers(FindAllUsersQuery query) {
        return new UserLookupResponse(repository.findAll());
    }

    @QueryHandler
    @Override
    public UserLookupResponse getUserById(FindUserByIdQuery query) {
        List<User> users = repository
                .findById(query.getId())
                .map(List::of)
                .orElseThrow(() -> new UserNotFoundException("User not found by id `" + query.getId() + "`"));
        return new UserLookupResponse(users);
    }

    @QueryHandler
    @Override
    public UserLookupResponse searchUsers(SearchUsersQuery query) {
        List<User> users = repository.findByFilterRegex(query.getFilter());
        return new UserLookupResponse(users);
    }
}
