package crypto.order.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderCreateResponse {

    private String orderId;
    private LocalDateTime createAt;
}
