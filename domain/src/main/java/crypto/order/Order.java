package crypto.order;

import crypto.BaseEntity;
import crypto.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.order.OrderSide.*;
import static crypto.order.OrderType.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

    @Id @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal filledQuantity = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime registeredDateTime;
    private LocalDateTime deletedDateTime;

    @Builder
    public Order(String symbol, BigDecimal price, BigDecimal quantity, BigDecimal totalPrice, BigDecimal totalAmount,
                 OrderType orderType, OrderSide orderSide, OrderStatus orderStatus, User user, LocalDateTime registeredDateTime) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.totalAmount = totalAmount;
        this.orderType = orderType;
        this.orderSide = orderSide;
        this.orderStatus = orderStatus;
        this.user = user;
        this.registeredDateTime = registeredDateTime;
    }

    public static Order createLimitOrder(String symbol, BigDecimal price, BigDecimal quantity,
                                         OrderSide orderSide, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .symbol(symbol)
                .price(price)
                .quantity(quantity)
                .orderType(LIMIT)
                .orderSide(orderSide)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public static Order createMarketBuyOrder(String symbol, BigDecimal totalPrice, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .symbol(symbol)
                .orderType(MARKET)
                .orderSide(BUY)
                .totalPrice(totalPrice)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public static Order createMarketSellOrder(String symbol, BigDecimal totalAmount, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .symbol(symbol)
                .orderType(MARKET)
                .orderSide(SELL)
                .totalAmount(totalAmount)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public void setDeleted(LocalDateTime deletedDateTime) {
        this.deletedDateTime = deletedDateTime;
    }

    public BigDecimal calculateRemainQuantity() {

        return (getQuantity().subtract(getFilledQuantity())).max(BigDecimal.ZERO);
    }
}
