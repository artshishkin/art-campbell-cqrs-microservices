package net.shyshkin.study.cqrs.user.query.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import org.axonframework.queryhandling.QueryExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.concurrent.CompletionException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class UserControllersExceptionHandlers {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public BaseResponse handle(Exception ex) {

        logException(ex);

        return new BaseResponse(ex.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(BAD_REQUEST)
    public BaseResponse handleValidationExceptions(Exception ex) {

        logException(ex);

        return new BaseResponse(ex.getMessage());
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<BaseResponse> handle(CompletionException ex) {

        logException(ex);

        if (ex.getCause() != null && ex.getCause() instanceof QueryExecutionException && ex.getCause().getMessage().contains("User not found")) {
            return ResponseEntity.status(NOT_FOUND).body(new BaseResponse(ex.getCause().getMessage()));
        }

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new BaseResponse(ex.getMessage()));
    }

    private void logException(Exception ex) {
        log.debug("Exception happened: {}:{}",
                ex.getClass().getName(),
                ex.getMessage()
        );
    }

}
