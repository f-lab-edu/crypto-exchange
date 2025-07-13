package crypto.order.service.order.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException() {
        super("존재하지 않는 주문번호입니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

