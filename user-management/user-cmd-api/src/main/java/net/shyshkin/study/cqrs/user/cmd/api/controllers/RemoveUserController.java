package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RemoveUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RemoveUserController {

    private final CommandGateway commandGateway;
    private final UserMapper mapper;

    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    public BaseResponse removeUser(@PathVariable String id) {

        var command = new RemoveUserCommand(id);
        commandGateway.sendAndWait(command);
        return new BaseResponse("User removed successfully");
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

    @ExceptionHandler(CommandExecutionException.class)
    public ResponseEntity<BaseResponse> handle(CommandExecutionException ex) {

        var status = (ex.getMessage().contains("not found")) ? NOT_FOUND : INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(status)
                .body(new BaseResponse(ex.getMessage()));
    }
}
