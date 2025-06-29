package crypto.trade.eventhandler.exception;

import crypto.common.entity.exception.BusinessException;

import org.springframework.http.HttpStatus;


public class TradeOrderNotFoundException extends BusinessException {

    public TradeOrderNotFoundException() {
        super("존재하지 않는 체결 주문번호입니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

