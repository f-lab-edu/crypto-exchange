package crypto.order;

import crypto.BaseEntity;
import crypto.coin.Coin;
import crypto.order.exception.FilledQuantityExceedException;
import crypto.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.order.OrderSide.*;
import static crypto.order.OrderStatus.*;
import static crypto.order.OrderType.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

    @Id @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal filledQuantity = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime registeredDateTime;
    private LocalDateTime deletedDateTime;

    @Builder
    public Order(BigDecimal price, BigDecimal quantity, BigDecimal totalPrice, BigDecimal totalAmount,
                 OrderType orderType, OrderSide orderSide, Coin coin, User user, LocalDateTime registeredDateTime) {
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.totalAmount = totalAmount;
        this.orderType = orderType;
        this.orderSide = orderSide;
        this.coin = coin;
        this.user = user;
        this.registeredDateTime = registeredDateTime;
    }

    public static Order createLimitOrder(BigDecimal price, BigDecimal quantity,
                                         OrderSide orderSide, Coin coin, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .price(price)
                .quantity(quantity)
                .orderType(LIMIT)
                .orderSide(orderSide)
                .coin(coin)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public static Order createMarketBuyOrder(BigDecimal totalPrice, Coin coin, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .orderType(MARKET)
                .orderSide(BUY)
                .totalPrice(totalPrice)
                .coin(coin)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public static Order createMarketSellOrder(BigDecimal totalAmount, Coin coin, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .orderType(MARKET)
                .orderSide(SELL)
                .totalAmount(totalAmount)
                .coin(coin)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public void fill(BigDecimal filledQuantity) {
        this.filledQuantity = this.filledQuantity.add(filledQuantity);
    }

    public boolean isFullyFilled() {
        return this.quantity.compareTo(this.filledQuantity) == 0;
    }

    public void markDeleted(LocalDateTime deletedDateTime) {
        this.deletedDateTime = deletedDateTime;
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
