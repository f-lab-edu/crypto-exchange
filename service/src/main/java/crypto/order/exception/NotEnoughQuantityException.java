package crypto.order.exception;

import crypto.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotEnoughQuantityException extends BusinessException {
    public NotEnoughQuantityException() {
        super("판매 가능한 수량이 부족합니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
