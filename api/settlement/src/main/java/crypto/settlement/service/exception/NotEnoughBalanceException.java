package crypto.settlement.service.exception;

import crypto.common.entity.exception.BusinessException;
import org.springframework.http.HttpStatus;


public class NotEnoughBalanceException extends BusinessException {
  public NotEnoughBalanceException() {
    super("사용 가능한 잔액이 부족합니다.");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}

