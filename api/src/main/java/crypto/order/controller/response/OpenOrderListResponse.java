package crypto.order.controller.response;

import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder
public class OpenOrderListResponse {

    private Long orderId;
    private String symbol;
    private OrderSide orderSide;
    private BigDecimal price;
    private BigDecimal requestQty;
    private BigDecimal remainQty;
    private LocalDateTime requestedAt;

    @Builder
    public OpenOrderListResponse(Long orderId, String symbol, OrderSide orderSide, BigDecimal price, BigDecimal requestQty, BigDecimal remainQty, LocalDateTime requestedAt) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.orderSide = orderSide;
        this.price = price;
        this.requestQty = requestQty;
        this.remainQty = remainQty;
        this.requestedAt = requestedAt;
    }

    public static OpenOrderListResponse of(Order order) {
        return OpenOrderListResponse.builder()
                .orderId(order.getId())
                .symbol(order.getCoin().getSymbol())
                .orderSide(order.getOrderSide())
                .price(order.getPrice())
                .requestQty(order.getQuantity())
                .remainQty(order.calculateRemainQuantity())
                .requestedAt(order.getRegisteredDateTime())
                .build();
    }
}
