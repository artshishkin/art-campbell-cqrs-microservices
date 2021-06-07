package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.UpdateUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UpdateUserController {

    private final CommandGateway commandGateway;
    private final UserMapper mapper;

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    public BaseResponse updateUser(@Valid @RequestBody UserCreateDto dto, @PathVariable String id) {

        User user = mapper.toModel(dto);
        user.setId(id);
        var command = UpdateUserCommand.builder()
                .user(user)
                .id(id)
                .build();
        commandGateway.sendAndWait(command);
        return new BaseResponse("User updated successfully");
    }
}
