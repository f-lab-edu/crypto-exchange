package crypto.settlement.entity.exception;

import crypto.common.entity.exception.DomainException;


public class LockedBalanceExceedException extends DomainException {
    public LockedBalanceExceedException() {
        super("잠금처리된 잔액 보다 큰 금액입니다.");
    }
}
