package net.shyshkin.study.cqrs.bankaccount.query.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.bankaccount.query.api.exceptions.NoContentException;
import org.axonframework.queryhandling.QueryExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import java.util.concurrent.CompletionException;

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

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse handle(EntityNotFoundException ex) {
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

    @ExceptionHandler(QueryExecutionException.class)
    public ResponseEntity<BaseResponse> handle(QueryExecutionException ex) {

        var status = INTERNAL_SERVER_ERROR;

        if (ex.getMessage().contains("not found")) {
            status = NOT_FOUND;
        }

        logException(ex);
        return ResponseEntity
                .status(status)
                .body(new BaseResponse(ex.getMessage()));
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<BaseResponse> handle(CompletionException ex) {

        var status = INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();
        if (ex.getCause() != null && ex.getCause() instanceof QueryExecutionException && ex.getCause().getMessage().contains("not found")) {
            status = NOT_FOUND;
            message = ex.getCause().getMessage();
        }

        logException(ex);
        return ResponseEntity
                .status(status)
                .body(new BaseResponse(message));
    }

    @ExceptionHandler(NoContentException.class)
    @ResponseStatus(NO_CONTENT)
    public void handle(NoContentException ex) {
        logException(ex);
    }
}
