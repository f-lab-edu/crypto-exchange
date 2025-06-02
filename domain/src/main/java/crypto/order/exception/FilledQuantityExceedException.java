package crypto.order.exception;

import crypto.exception.DomainException;

public class FilledQuantityExceedException extends DomainException {
    public FilledQuantityExceedException() {
        super("체결 수량이 전체 주문 수량을 초과했습니다.");
    }
}
