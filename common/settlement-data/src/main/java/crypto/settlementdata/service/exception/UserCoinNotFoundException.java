package crypto.settlementdata.service.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class UserCoinNotFoundException extends BusinessException {
    public UserCoinNotFoundException() {
        super("유저가 해당 코인을 보유하고 있지 않습니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
