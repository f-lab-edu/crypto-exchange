package crypto.trade.eventhandler.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class TradeNotFoundException extends BusinessException {

    public TradeNotFoundException() {
        super("존재하지 않는 체결 번호입니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

