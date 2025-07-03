package crypto.order.controller.order.response;

import crypto.order.entity.order.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class OrderDeleteResponse {

    private Long orderId;
    private LocalDateTime deletedAt;

    @Builder
    public OrderDeleteResponse(Long orderId, LocalDateTime deletedAt) {
        this.orderId = orderId;
        this.deletedAt = deletedAt;
    }

    public static OrderDeleteResponse of(Order order) {
        return OrderDeleteResponse.builder()
                .orderId(order.getId())
                .deletedAt(order.getRegisteredDateTime())
                .build();
    }
}
