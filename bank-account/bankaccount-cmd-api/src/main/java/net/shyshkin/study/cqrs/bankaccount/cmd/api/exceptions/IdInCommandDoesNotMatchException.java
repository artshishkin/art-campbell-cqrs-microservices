package net.shyshkin.study.cqrs.bankaccount.cmd.api.exceptions;

public class IdInCommandDoesNotMatchException extends RuntimeException {

    public IdInCommandDoesNotMatchException(String message) {
        super(message);
    }
}
