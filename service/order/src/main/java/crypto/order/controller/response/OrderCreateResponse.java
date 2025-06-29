package crypto.order.controller.response;

import crypto.order.entity.order.Order;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;


@Getter
public class OrderCreateResponse {

    private Long orderId;
    private LocalDateTime createAt;

    @Builder
    public OrderCreateResponse(Long orderId, LocalDateTime createAt) {
        this.orderId = orderId;
        this.createAt = createAt;
    }

    public static OrderCreateResponse of(Order order) {
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .createAt(order.getRegisteredDateTime())
                .build();
    }
}
