package crypto.order.service.coin.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class CoinNotFoundException extends BusinessException {
    public CoinNotFoundException() {
        super("존재하지 않는 코인입니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
