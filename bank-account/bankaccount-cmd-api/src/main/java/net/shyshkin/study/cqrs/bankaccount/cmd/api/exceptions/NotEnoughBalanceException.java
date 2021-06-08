package net.shyshkin.study.cqrs.bankaccount.cmd.api.exceptions;

public class NotEnoughBalanceException extends RuntimeException{

    public NotEnoughBalanceException(String message) {
        super(message);
    }
}
