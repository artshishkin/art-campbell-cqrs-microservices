package net.shyshkin.study.cqrs.bankaccount.cmd.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.exceptions.IdInCommandDoesNotMatchException;
import net.shyshkin.study.cqrs.bankaccount.core.dto.BaseResponse;
import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse handle(MethodArgumentNotValidException ex) {
        logException(ex);
        return new BaseResponse(ex.getMessage());
    }

    @ExceptionHandler(IdInCommandDoesNotMatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse handle(IdInCommandDoesNotMatchException ex) {
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

    @ExceptionHandler(CommandExecutionException.class)
    public ResponseEntity<BaseResponse> handle(CommandExecutionException ex) {

        var status = INTERNAL_SERVER_ERROR;

        if (ex.getMessage().contains("not found")) {
            status = NOT_FOUND;
        } else if (ex.getMessage().contains("Withdrawal declined, insufficient funds")) {
            status = BAD_REQUEST;
        }

        logException(ex);
        return ResponseEntity
                .status(status)
                .body(new BaseResponse(ex.getMessage()));
    }
}
