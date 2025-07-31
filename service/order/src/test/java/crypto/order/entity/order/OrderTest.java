package crypto.order.entity.order;

import crypto.order.entity.coin.Coin;
import crypto.order.entity.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static crypto.order.entity.order.OrderSide.*;
import static crypto.order.entity.order.OrderType.*;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;


class OrderTest {

    private LocalDateTime registeredDateTime;
    private User user;
    private Coin coin;

    @BeforeEach
    void setUp() {
        registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        user = User.createUser("test@email.com");
        coin = Coin.create("XRP", "Ripple");
    }

    @DisplayName("지정가 주문 생성시 오더타입은 LIMIT 이다.")
    @Test
    void checkLimit() {

        // when
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(LIMIT);
    }

    @DisplayName("시장가 매수 주문 생성시 오더타입은 MARKET 이다.")
    @Test
    void checkBuyMarket() {

        // when
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(MARKET);
    }

    @DisplayName("시장가 매도 주문 생성시 오더타입은 MARKET 이다.")
    @Test
    void checkSellMarket() {

        // when
        Order order = Order.createMarketSellOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(MARKET);
    }

    @DisplayName("시장가 매수 주문 생성시 오더사이드는 BUY 이다.")
    @Test
    void checkBuy() {

        // when
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderSide()).isEqualByComparingTo(BUY);
    }

    @DisplayName("시장가 매도 주문 생성시 오더사이드는 SELL 이다.")
    @Test
    void checkSell() {

        // when
        Order order = Order.createMarketSellOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderSide()).isEqualByComparingTo(SELL);
    }

    @DisplayName("지정가 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkLimitDeletedDateTime() {

        // when
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("시장가 매수 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkMarketBuyDeletedDateTime() {

        // when
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("시장가 매도 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkMarketSellDeletedDateTime() {

        // when
        Order order = Order.createMarketSellOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }
}