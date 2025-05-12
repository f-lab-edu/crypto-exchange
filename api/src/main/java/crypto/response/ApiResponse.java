package crypto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import static crypto.CommonResponseCode.*;

@Getter
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

    public static <T> ApiResponse<PageResponse<T>> successPage(Page<T> page) {
        return success(PageResponse.from(page));
    }
}


