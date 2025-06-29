package crypto.order.controller.response;

import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder
public class CompleteOrderListResponse {

    private Long orderId;
    private String symbol;
    private OrderSide orderSide;
    private BigDecimal price;
    private BigDecimal amount;
    private LocalDateTime completedAt;

    @Builder
    public CompleteOrderListResponse(Long orderId, String symbol, OrderSide orderSide, BigDecimal price, BigDecimal amount, LocalDateTime completedAt) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.orderSide = orderSide;
        this.price = price;
        this.amount = amount;
        this.completedAt = completedAt;
    }

    public static CompleteOrderListResponse of(Order order) {
        return CompleteOrderListResponse.builder()
                .orderId(order.getId())
                .symbol(order.getCoin().getSymbol())
                .orderSide(order.getOrderSide())
                .price(order.getPrice())
                .amount(order.getQuantity())
                .completedAt(order.getRegisteredDateTime())
                .build();
    }
}
