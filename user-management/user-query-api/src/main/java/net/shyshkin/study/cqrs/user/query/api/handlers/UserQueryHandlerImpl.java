package net.shyshkin.study.cqrs.user.query.api.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.models.Account;
import net.shyshkin.study.cqrs.user.core.models.User;
import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.UserProviderResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.VerificationPasswordResponse;
import net.shyshkin.study.cqrs.user.query.api.exceptions.UserNotFoundException;
import net.shyshkin.study.cqrs.user.query.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.query.api.queries.*;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryHandlerImpl implements UserQueryHandler {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

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
    public UserProviderResponse getUserByEmail(FindUserByEmailQuery query) {
        return repository
                .findByEmailAddress(query.getEmail())
                .map(mapper::toProviderResponse)
                .orElse(null);
    }

    @QueryHandler
    @Override
    public VerificationPasswordResponse verifyEmailAndPassword(VerifyEmailPasswordQuery query) {
        Boolean passwordValid = repository
                .findByEmailAddress(query.getEmail())
                .map(user -> passwordEncoder.matches(query.getPassword(), user.getAccount().getPassword()))
                .orElse(false);

        return new VerificationPasswordResponse(passwordValid);
    }

    @QueryHandler
    @Override
    public UserProviderResponse getUserByUsername(FindUserByUsernameQuery query) {

//        return findUserByUsernameUsingExample(query);
        return findUserByUsernameUsingQuery(query);
    }

    private UserProviderResponse findUserByUsernameUsingExample(FindUserByUsernameQuery query) {
        String username = query.getUsername();
        Account accountExample = Account.builder().username(username).build();
        User userExample = User.builder().account(accountExample).build();

        return repository.findOne(Example.of(userExample))
                .map(mapper::toProviderResponse)
                .orElse(null);
    }

    private UserProviderResponse findUserByUsernameUsingQuery(FindUserByUsernameQuery query) {
        String username = query.getUsername();
        return repository.findUserByUsername(username)
                .map(mapper::toProviderResponse)
                .orElse(null);
    }

    @QueryHandler
    @Override
    public VerificationPasswordResponse verifyUsernameAndPassword(VerifyUsernamePasswordQuery query) {
        Boolean passwordValid = repository
                .findUserByUsername(query.getUsername())
                .map(user -> passwordEncoder.matches(query.getPassword(), user.getAccount().getPassword()))
                .orElse(false);

        return new VerificationPasswordResponse(passwordValid);
    }

    @QueryHandler
    @Override
    public UserLookupResponse searchUsers(SearchUsersQuery query) {
        List<User> users = repository.findByFilterRegex(query.getFilter());
        return new UserLookupResponse(users);
    }
}
