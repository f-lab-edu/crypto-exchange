package crypto.order;

import crypto.coin.Coin;
import crypto.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.order.OrderSide.BUY;
import static crypto.order.OrderSide.SELL;
import static crypto.order.OrderType.LIMIT;
import static crypto.order.OrderType.MARKET;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@Transactional
class OrderTest {

    @DisplayName("지정가 주문 생성시 오더타입은 LIMIT 이다.")
    @Test
    void checkLimit() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(LIMIT);
    }

    @DisplayName("시장가 매수 주문 생성시 오더타입은 MARKET 이다.")
    @Test
    void checkBuyMarket() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(MARKET);
    }

    @DisplayName("시장가 매도 주문 생성시 오더타입은 MARKET 이다.")
    @Test
    void checkSellMarket() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createMarketSellOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(MARKET);
    }

    @DisplayName("시장가 매수 주문 생성시 오더사이드는 BUY 이다.")
    @Test
    void checkBuy() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderSide()).isEqualByComparingTo(BUY);
    }

    @DisplayName("시장가 매도 주문 생성시 오더사이드는 SELL 이다.")
    @Test
    void checkSell() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createMarketSellOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getOrderSide()).isEqualByComparingTo(SELL);
    }

    @DisplayName("지정가 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkLimitDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("시장가 매수 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkMarketBuyDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("시장가 매도 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkMarketSellDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");

        // when
        Order order = Order.createMarketSellOrder(valueOf(100), coin, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("주문 체결 시 해당 주문의 체결 된 수량이 증가한다.")
    @Test
    void checkFilledQuantity() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // when
        order.fill(valueOf(5));

        // then
        assertThat(order.getFilledQuantity()).isEqualByComparingTo(valueOf(5));
    }

    @DisplayName("해당 주문의 요청 수량과 체결된 수량을 비교하여 체결 완료된 주문을 확인한다.")
    @Test
    void checkFullyFilledTrue() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // when
        order.fill(valueOf(10));

        // then
        assertThat(order.isFullyFilled()).isTrue();
    }

    @DisplayName("해당 주문의 요청 수량과 체결된 수량을 비교하여 체결 완료 되지 않은 주문을 확인한다.")
    @Test
    void checkFullyFilledFalse() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        // when
        order.fill(valueOf(9));

        // then
        assertThat(order.isFullyFilled()).isFalse();
    }

    @DisplayName("주문 삭제시 주문의 deletedDateTime 에 시간이 설정된다.")
    @Test
    void checkDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");
        Order order = Order.createMarketBuyOrder(valueOf(100), coin, user, registeredDateTime);

        // when
        order.markDeleted(registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNotNull();
    }

    @DisplayName("미체결 주문 조회시 남은 주문 수량을 계산한다.")
    @Test
    void checkRemainQuantity() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        order.fill(valueOf(9));

        // when
        BigDecimal remainQuantity = order.calculateRemainQuantity();

        // then
        assertThat(remainQuantity).isEqualByComparingTo(valueOf(1));
    }

    @DisplayName("미체결 주문 조회시 남은 주문 수량이 음수가 되는 경우 0을 반환한다.")
    @Test
    void checkRemainQuantityNegative() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Coin coin = Coin.create("XRP", "Ripple");
        Order order = Order.createLimitOrder(valueOf(100), valueOf(10), BUY, coin, user, registeredDateTime);

        order.fill(valueOf(11));

        // when
        BigDecimal remainQuantity = order.calculateRemainQuantity();

        // then
        assertThat(remainQuantity).isEqualByComparingTo(valueOf(0));
    }
}