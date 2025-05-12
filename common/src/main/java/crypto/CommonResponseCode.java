package crypto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonResponseCode {

    SUCCESS("요청이 정상적으로 처리되었습니다.");

    private final String message;
}
