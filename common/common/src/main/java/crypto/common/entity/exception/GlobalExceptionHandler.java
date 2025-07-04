package crypto.common.entity.exception;

import crypto.common.api.response.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Object> handleBusinessException(BusinessException e) {
        return ApiResponse.of(
                e.getStatus(),
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(DomainException.class)
    public ApiResponse<Object> handleDomainException(DomainException e) {
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                null
        );
    }

}

