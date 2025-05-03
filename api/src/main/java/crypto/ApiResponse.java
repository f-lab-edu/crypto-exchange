package crypto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import static crypto.order.OrderResponseCode.*;

@Getter
public class ApiResponse<T> {

    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public ApiResponse(int code, HttpStatus status, String message, T data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS.getCode(), HttpStatus.OK, SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<PageResponse<T>> successPage(Page<T> page) {
        return success(PageResponse.from(page));
    }
}


