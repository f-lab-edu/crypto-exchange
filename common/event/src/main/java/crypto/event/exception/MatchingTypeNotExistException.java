package crypto.event.exception;

import crypto.common.entity.exception.DomainException;


public class MatchingTypeNotExistException extends DomainException {

    public MatchingTypeNotExistException() {
        super("매칭되는 이벤트 타입이 존재하지 않습니다.");
    }
}
