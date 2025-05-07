package crypto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonResponseCode {

    //common
    SUCCESS(200, "요청이 정상적으로 처리되었습니다."),
    FAIL(400, "요청이 실패하였습니다."),

    // order
    ORDER_NOT_FOUND(404, "존재하지 않는 주문번호입니다."),

    // user
    USER_NOT_FOUND(404, "존재하지 않는 유저입니다.");

    private final int code;
    private final String message;
}
