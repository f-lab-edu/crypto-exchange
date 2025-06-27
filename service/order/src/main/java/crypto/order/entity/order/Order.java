package crypto.order.entity.order;

import crypto.common.entity.BaseEntity;
import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.exception.FilledQuantityExceedException;
import crypto.order.entity.user.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.order.entity.order.OrderSide.*;
import static crypto.order.entity.order.OrderStatus.*;
import static crypto.order.entity.order.OrderType.*;

import static java.math.BigDecimal.ZERO;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

    @Id @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price = ZERO;
    private BigDecimal quantity = ZERO;
    private BigDecimal filledQuantity = ZERO;
    private BigDecimal marKetTotalPrice = ZERO;
    private BigDecimal marketTotalQuantity = ZERO;

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
    public Order(BigDecimal price, BigDecimal quantity, BigDecimal totalPrice, BigDecimal totalQuantity,
                 OrderType orderType, OrderSide orderSide, Coin coin, User user, LocalDateTime registeredDateTime) {
        this.price = price;
        this.quantity = quantity;
        this.marKetTotalPrice = totalPrice;
        this.marketTotalQuantity = totalQuantity;
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

    public static Order createMarketSellOrder(BigDecimal totalQuantity, Coin coin, User user, LocalDateTime registeredDateTime) {
        return Order.builder()
                .orderType(MARKET)
                .orderSide(SELL)
                .totalQuantity(totalQuantity)
                .coin(coin)
                .user(user)
                .registeredDateTime(registeredDateTime)
                .build();
    }

    public void markDeleted(LocalDateTime deletedDateTime) {
        this.deletedDateTime = deletedDateTime;
    }

    public BigDecimal calculateRemainQuantity() {
        if (getFilledQuantity().compareTo(getQuantity()) > 0) {
            throw new FilledQuantityExceedException();
        }

        return (getQuantity().subtract(getFilledQuantity()));
    }
}
