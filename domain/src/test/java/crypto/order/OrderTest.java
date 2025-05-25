package crypto.order;

import crypto.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTest(classes = crypto.AppApiApplication.class)
class OrderTest {

    @DisplayName("지정가 주문 생성시 오더타입은 LIMIT 이다.")
    @Test
    void checkLimit() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createLimitOrder("BTC", valueOf(100), valueOf(10), BUY, user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(LIMIT);
    }

    @DisplayName("시장가 매수 주문 생성시 오더타입은 MARKET 이다.")
    @Test
    void checkBuyMarket() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createMarketBuyOrder("BTC", valueOf(100), user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(MARKET);
    }

    @DisplayName("시장가 매도 주문 생성시 오더타입은 MARKET 이다.")
    @Test
    void checkSellMarket() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createMarketSellOrder("BTC", valueOf(100), user, registeredDateTime);

        // then
        assertThat(order.getOrderType()).isEqualByComparingTo(MARKET);
    }

    @DisplayName("시장가 매수 주문 생성시 오더사이드는 BUY 이다.")
    @Test
    void checkBuy() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createMarketBuyOrder("BTC", valueOf(100), user, registeredDateTime);

        // then
        assertThat(order.getOrderSide()).isEqualByComparingTo(BUY);
    }

    @DisplayName("시장가 매도 주문 생성시 오더사이드는 SELL 이다.")
    @Test
    void checkSell() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createMarketSellOrder("BTC", valueOf(100), user, registeredDateTime);

        // then
        assertThat(order.getOrderSide()).isEqualByComparingTo(SELL);
    }

    @DisplayName("지정가 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkLimitDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createLimitOrder("BTC", valueOf(100), valueOf(10), BUY, user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("시장가 매수 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkMarketBuyDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createMarketBuyOrder("BTC", valueOf(100), user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("시장가 매도 주문 생성시 deletedDateTime 은 null 이다.")
    @Test
    void checkMarketSellDeletedDateTime() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));

        // when
        Order order = Order.createMarketSellOrder("BTC", valueOf(100), user, registeredDateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNull();
    }

    @DisplayName("주문 삭제시 주문의 deletedDateTime 에 시간이 설정된다.")
    @Test
    void checkDeletedDateTime() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Order order = Order.createMarketBuyOrder("BTC", valueOf(100), user, dateTime);

        // when
        order.markDeleted(dateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNotNull();
    }

    @DisplayName("유저의 사용 가능 금액을 조회한다.")
    @Test
    void checkAvailableBalance() {
        // given
        User user = User.createUser("test@email.com", valueOf(1000));

        // when // then
        assertThat(user.getAvailableBalance()).isEqualByComparingTo(valueOf(1000));
    }

    @DisplayName("미체결 주문 조회시 남은 주문 수량을 계산한다.")
    @Test
    void checkRemainQuantity() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000));
        Order order = Order.createLimitOrder("BTC", valueOf(100), valueOf(10), BUY, user, registeredDateTime);

        // when
        BigDecimal remainQuantity = order.calculateRemainQuantity();

        // then
        assertThat(remainQuantity).isEqualByComparingTo(valueOf(10));
    }
}