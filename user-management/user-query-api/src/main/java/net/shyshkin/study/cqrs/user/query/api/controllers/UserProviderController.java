package net.shyshkin.study.cqrs.user.query.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.query.api.dto.UserProviderResponse;
import net.shyshkin.study.cqrs.user.query.api.queries.FindUserByEmailQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Email;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/provider")
@RequiredArgsConstructor
@Validated
public class UserProviderController {

    private final QueryGateway queryGateway;

    @GetMapping("/email/{email}")
    public ResponseEntity<UserProviderResponse> getUserByEmail(
            @PathVariable @Email(message = "Provide correct email address") String email) {
        var query = new FindUserByEmailQuery(email);
        UserProviderResponse userProviderResponse = queryGateway.query(query, UserProviderResponse.class).join();
        if (userProviderResponse == null)
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(userProviderResponse);
    }

}
