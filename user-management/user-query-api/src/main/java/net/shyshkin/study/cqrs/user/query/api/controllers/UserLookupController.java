package net.shyshkin.study.cqrs.user.query.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.queries.FindAllUsersQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByIdQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.SearchUsersQuery;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserLookupController {

    private final QueryGateway queryGateway;

    @GetMapping
    public ResponseEntity<UserLookupResponse> getAllUsers() {
        var query = new FindAllUsersQuery();
        UserLookupResponse response = queryGateway.query(query, UserLookupResponse.class).join();
        if (response == null || response.getUsers() == null)
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public UserLookupResponse getUserById(@PathVariable String id) {
        var query = new FindUserByIdQuery(id);
        return queryGateway.query(query, UserLookupResponse.class).join();
    }

    @GetMapping("/search/{filter}")
    public ResponseEntity<UserLookupResponse> searchUsers(@PathVariable String filter) {
        var query = new SearchUsersQuery(filter);
        UserLookupResponse lookupResponse = queryGateway.query(query, UserLookupResponse.class).join();
        if (lookupResponse.getUsers() == null || lookupResponse.getUsers().isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lookupResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public BaseResponse handle(Exception ex) {
        log.debug("Exception happened: {}:{}",
                ex.getClass().getName(),
                ex.getMessage()
        );
        return new BaseResponse(ex.getMessage());
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<BaseResponse> handle(CompletionException ex) {
        log.debug("Exception happened: {}:{}",
                ex.getClass().getName(),
                ex.getMessage()
        );
        if (ex.getCause() != null && ex.getCause() instanceof QueryExecutionException && ex.getCause().getMessage().contains("User not found")) {
            return ResponseEntity.status(NOT_FOUND).body(new BaseResponse(ex.getCause().getMessage()));
        }

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new BaseResponse(ex.getMessage()));
    }
}
