package crypto.common.api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.http.HttpStatus;

import static crypto.common.api.response.CommonResponseCode.SUCCESS;


@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public ApiResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(httpStatus, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return of(HttpStatus.OK, SUCCESS.getMessage(), data);
    }
}


