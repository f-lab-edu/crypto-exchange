package crypto.settlementdata.service.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class UserBalanceNotFoundException extends BusinessException {

    public UserBalanceNotFoundException() {
        super("유저 잔액 엔티티가 존재하지 않습니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

