package net.shyshkin.study.cqrs.user.query.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.queries.FindAllUsersQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByIdQuery;
import net.shyshkin.study.cqrs.user.query.api.queries.SearchUsersQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserLookupController {

    private final QueryGateway queryGateway;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    public ResponseEntity<UserLookupResponse> getAllUsers() {
        var query = new FindAllUsersQuery();
        UserLookupResponse response = queryGateway.query(query, UserLookupResponse.class).join();
        if (response == null || response.getUsers() == null)
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    public UserLookupResponse getUserById(@PathVariable String id) {
        var query = new FindUserByIdQuery(id);
        return queryGateway.query(query, UserLookupResponse.class).join();
    }

    @GetMapping("/search/{filter}")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    public ResponseEntity<UserLookupResponse> searchUsers(@PathVariable String filter) {
        var query = new SearchUsersQuery(filter);
        UserLookupResponse lookupResponse = queryGateway.query(query, UserLookupResponse.class).join();
        if (lookupResponse.getUsers() == null || lookupResponse.getUsers().isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lookupResponse);
    }
}
