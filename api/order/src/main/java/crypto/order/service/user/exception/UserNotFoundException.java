package crypto.order.service.user.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super("존재하지 않는 유저입니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
