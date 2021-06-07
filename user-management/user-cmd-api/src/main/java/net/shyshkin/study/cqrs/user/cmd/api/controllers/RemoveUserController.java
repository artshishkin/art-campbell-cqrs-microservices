package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RemoveUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RemoveUserController {

    private final CommandGateway commandGateway;
    private final UserMapper mapper;

    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    public BaseResponse removeUser(@PathVariable String id) {

        var command = new RemoveUserCommand(id);
        commandGateway.sendAndWait(command);
        return new BaseResponse("User removed successfully");
    }
}
