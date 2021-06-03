package net.shyshkin.study.cqrs.user.query.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.queries.FindAllUsersQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByIdQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.SearchUsersQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search")
    public UserLookupResponse searchUsers(@RequestBody String filter) {
        var query = new SearchUsersQuery(filter);
        return queryGateway.query(query, UserLookupResponse.class).join();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse handle(Exception ex) {
        log.debug("Exception happened: {}:{}",
                ex.getClass().getName(),
                ex.getMessage()
        );
        return new BaseResponse(ex.getMessage());
    }
}
