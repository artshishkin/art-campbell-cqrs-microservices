package net.shyshkin.study.cqrs.bankaccount.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.CloseAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.OpenAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.bankaccount.core.dto.OpenAccountResponse;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class BankAccountCommandController {

    private final CommandGateway commandGateway;

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    public ResponseEntity<OpenAccountResponse> createAccount(@Valid @RequestBody OpenAccountCommand openAccountCommand) {

        UUID id = UUID.randomUUID();
        openAccountCommand.setId(id);
        commandGateway.send(openAccountCommand);
        OpenAccountResponse response = OpenAccountResponse.builder()
                .id(id)
                .message("Account opened successfully")
                .build();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment(id.toString())
                .build()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse closeAccount(@PathVariable UUID id) {
        commandGateway.sendAndWait(new CloseAccountCommand(id));
        return new BaseResponse("Account closed successfully");
    }
}
