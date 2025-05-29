package crypto.user.exception;

import crypto.exception.DomainException;

public class InsufficientAvailableBalanceException extends DomainException {
    public InsufficientAvailableBalanceException() {
        super("사용가능한 잔액이 부족합니다.");
    }
}
