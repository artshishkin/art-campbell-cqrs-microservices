package net.shyshkin.study.cqrs.bankaccount.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.DepositFundsCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.exceptions.IdInCommandDoesNotMatchException;
import net.shyshkin.study.cqrs.bankaccount.core.dto.BaseResponse;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{id}")
@RequiredArgsConstructor
public class FundsCommandController {

    private final CommandGateway commandGateway;

    @PutMapping("/deposits")
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    public BaseResponse depositFunds(
            @PathVariable UUID id,
            @Valid @RequestBody DepositFundsCommand depositFundsCommand) {

        if (!Objects.equals(depositFundsCommand.getId(), id))
            throw new IdInCommandDoesNotMatchException(String.format("Account Id in URL `%s` does not match Id in command `%s`", depositFundsCommand.getId(), id));

        commandGateway.send(depositFundsCommand);
        return new BaseResponse("Funds deposited successfully");
    }
}
