package crypto.settlement.service.exception;

import crypto.common.entity.exception.BusinessException;

import org.springframework.http.HttpStatus;


public class SettlementServiceException extends BusinessException {

    public SettlementServiceException() {
        super("Settlement 서비스와 통신에 실패하였습니다.");
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
