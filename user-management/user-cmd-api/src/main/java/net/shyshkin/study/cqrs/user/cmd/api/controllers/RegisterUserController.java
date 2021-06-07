package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.dto.RegisterUserResponse;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RegisterUserController {

    private final CommandGateway commandGateway;
    private final UserMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    public RegisterUserResponse registerUser(@Valid @RequestBody UserCreateDto dto) {

        User user = mapper.toModel(dto);

        var command = RegisterUserCommand.builder()
                .user(user)
                .id(user.getId())
                .build();
        String aggregateId = commandGateway.sendAndWait(command);
        return new RegisterUserResponse(aggregateId, "User registered successfully");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse handle(MethodArgumentNotValidException ex) {
        logException(ex);
        return new BaseResponse(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse handle(Exception ex) {
        logException(ex);
        return new BaseResponse(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse handle(AccessDeniedException ex) {
        logException(ex);
        throw ex;
    }

    private void logException(Exception ex) {
        log.debug("Exception happened: {}:{}",
                ex.getClass().getName(),
                ex.getMessage()
        );
    }
}
