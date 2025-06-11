package crypto.order;

import crypto.coin.Coin;
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
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TimeProvider timeProvider;


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
        Order order = orderQueryService.findOrder(orderResponse.getOrderId());

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

        Coin coin = Coin.create("XRP", "Ripple");
        Order order = orderRepository.save(createOrder(coin, valueOf(20000)));

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

    private Order createOrder(Coin coin, BigDecimal price) {
        return Order.builder()
                .coin(coin)
                .price(price)
                .build();
    }
}