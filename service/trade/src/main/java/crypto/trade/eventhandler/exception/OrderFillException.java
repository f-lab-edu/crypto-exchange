package crypto.trade.eventhandler.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class OrderFillException extends BusinessException {

    public OrderFillException() {
        super("주문의 남은 수량이 체결 수량보다 부족합니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

