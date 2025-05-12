package crypto.order;

import crypto.exception.BusinessException;
import crypto.order.exception.OrderNotFoundException;
import crypto.order.request.LimitOrderServiceRequest;
import crypto.order.response.OrderCreateResponse;
import crypto.time.TimeProvider;
import crypto.user.User;
import crypto.user.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static crypto.order.OrderSide.*;
import static crypto.order.OrderType.*;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = crypto.AppApiApplication.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TimeProvider timeProvider;


    @DisplayName("지정가 주문 생성시 오더타입은 LIMIT 이다.")
    @Test
    void checkLimit() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

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
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));
        Order order = Order.createMarketBuyOrder("BTC", valueOf(100), user, dateTime);

        // when
        order.setDeleted(dateTime);

        // then
        assertThat(order.getDeletedDateTime()).isNotNull();
    }

    @DisplayName("유저의 사용 가능 금액을 조회한다.")
    @Test
    void checkAvailableBalance() {
        // given
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));

        // when // then
        assertThat(user.getAvailableBalance()).isEqualByComparingTo(valueOf(1000));
    }

    @DisplayName("미체결 주문 조회시 남은 주문 수량을 계산한다.")
    @Test
    void checkRemainQuantity() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        User user = User.createUser("test@email.com", valueOf(1000), valueOf(1000));
        Order order = Order.createLimitOrder("BTC", valueOf(100), valueOf(10), BUY, user, registeredDateTime);

        // when
        BigDecimal remainQuantity = order.calculateRemainQuantity();

        // then
        assertThat(remainQuantity).isEqualByComparingTo(valueOf(10));
    }

    @DisplayName("지정가 매수 주문을 생성한다.")
    @Test
    void createLimitBuyOrder() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);

        User user = userRepository.save(createUser("test@email.com"));
        getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), null, Collections.emptyList())
        );

        LimitOrderServiceRequest request = LimitOrderServiceRequest.builder()
                .symbol("BTC")
                .price(valueOf(20000))
                .quantity(valueOf(20))
                .build();

        // when
        OrderCreateResponse orderResponse = orderService.createLimitBuyOrder(request);
        Order order = orderRepository.findById(orderResponse.getOrderId())
                .orElseThrow(OrderNotFoundException::new);

        // then
        assertThat(orderResponse.getOrderId()).isNotNull();
        assertThat(orderResponse.getCreateAt()).isEqualTo(registeredDateTime);
        assertThat(order)
                .extracting("symbol", "price", "quantity", "orderType", "orderSide", "user", "registeredDateTime")
                .contains("BTC", valueOf(20000), valueOf(20), LIMIT, BUY, user, registeredDateTime);
    }

    @DisplayName("선택한 주문을 삭제한다.")
    @Test
    void deleteOrder() {
        // given
        LocalDateTime deletedDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(deletedDateTime);

        Order order = orderRepository.save(createOrder("BTC", valueOf(20000)));

        // when
        orderService.deleteOrder(order.getId());

        // then
        assertThat(order.getDeletedDateTime()).isNotNull();
    }

    @DisplayName("존재하지 않는 주문번호로 삭제 요청 시 예외가 발생한다.")
    @Test
    void deleteOrderWithNotExistOrder() {
        // given
        LocalDateTime deletedDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(deletedDateTime);

        Long orderId = 123L;

        // when // then
        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 주문번호입니다.");
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .build();
    }

    private Order createOrder(String symbol, BigDecimal price) {
        return Order.builder()
                .symbol(symbol)
                .price(price)
                .build();
    }
}