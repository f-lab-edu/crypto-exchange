package crypto.trade.entity;

import crypto.trade.entity.exception.FilledQuantityExceedException;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.trade.entity.TradeOrderStatus.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "trade_orders")
public class TradeOrder {

    @Id
    @Column(name = "trade_order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private Long userId;

    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal filledQuantity;

    private TradeOrderSide orderSide;
    private TradeOrderStatus orderStatus = OPEN;

    private LocalDateTime registeredDateTime;
    private LocalDateTime deletedDateTime;

    @Builder
    public TradeOrder(Long orderId, Long userId, String symbol, BigDecimal price, BigDecimal quantity,
                      TradeOrderSide orderSide, LocalDateTime registeredDateTime) {
        this.orderId = orderId;
        this.userId = userId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.orderSide = orderSide;
        this.registeredDateTime = registeredDateTime;
    }

    public static TradeOrder create(Long orderId, Long userId, String symbol, BigDecimal price, BigDecimal quantity,
                                    TradeOrderSide orderSide, LocalDateTime registeredDateTime) {
        return TradeOrder.builder()
                .orderId(orderId)
                .userId(userId)
                .symbol(symbol)
                .price(price)
                .quantity(quantity)
                .orderSide(orderSide)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public boolean isFullyFilled() {
        return this.quantity.compareTo(this.filledQuantity) == 0;
    }

    public void fill(BigDecimal filledQuantity) {
        this.filledQuantity = this.filledQuantity.add(filledQuantity);
    }

    public void markCompleted() {
        this.orderStatus = FILLED;
    }

    public BigDecimal calculateRemainQuantity() {
        if (getFilledQuantity().compareTo(getQuantity()) > 0) {
            throw new FilledQuantityExceedException();
        }

        return (getQuantity().subtract(getFilledQuantity()));
    }
}
