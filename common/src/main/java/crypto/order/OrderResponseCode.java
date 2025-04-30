package crypto.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderResponseCode {

    SUCCESS(200, "요청이 정상적으로 처리되었습니다."),
    FAIL(400, "요청이 실패하였습니다.");

    private final int code;
    private final String message;
}
