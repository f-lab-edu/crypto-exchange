package crypto.order.service.order;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.eventsender.TradeEventSender;
import crypto.event.payload.EventPayload;
import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderProcessedEvent;
import crypto.order.entity.order.OrderSide;
import crypto.order.entity.user.User;
import crypto.order.repository.order.OrderProcessedEventDbRepository;
import crypto.order.repository.order.OrderProcessedEventRepository;
import crypto.order.repository.order.OrderRepository;
import crypto.order.service.coin.CoinService;
import crypto.order.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static crypto.event.EventType.*;
import static crypto.event.EventType.LIMIT_BUY_ORDER_CREATE;
import static crypto.order.entity.order.OrderType.*;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderEventServiceTest {

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private OrderProcessedEventRepository orderProcessedEventRepository;

    @Mock
    private OrderProcessedEventDbRepository orderProcessedEventDbRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeEventSender tradeEventSender;

    @Mock
    private CoinService coinService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderEventService orderEventService;

    private EventPayload payload;
    private Event event;
    private Coin coin;
    private User user;

    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        payload = EventPayload.builder()
                .userId(42L)
                .symbol("BTC")
                .price(valueOf(1000))
                .quantity(valueOf(5))
                .orderSide(OrderSide.BUY.name())
                .build();

        event = Event.of("a1b2c3d4-e5f6-7890-1234-567890ab1def", LIMIT_BUY_ORDER_CREATE, payload);
        coin = new Coin("BTC", "BitCoin");
        user = new User("test@email.com");
    }

    @DisplayName("새로운 이벤트가 성공적으로 처리되고 주문이 생성된다.")
    @Test
    void handleEventSuccess() {
        // given
        when(orderProcessedEventRepository.setIfAbsent(event.getEventId())).thenReturn(true);
        when(timeProvider.now()).thenReturn(NOW);
        when(coinService.getCoinOrThrow(payload.getSymbol())).thenReturn(coin);
        when(userService.getUserOrThrow(payload.getUserId())).thenReturn(user);

        // when
        orderEventService.handleEvent(event);

        // then
        verify(orderProcessedEventRepository, times(1)).setIfAbsent(event.getEventId());
        verify(timeProvider, times(1)).now();
        verify(coinService, times(1)).getCoinOrThrow(payload.getSymbol());
        verify(userService, times(1)).getUserOrThrow(payload.getUserId());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        verify(tradeEventSender, times(1)).send(eq(LIMIT_BUY_ORDER_TRADE), eq(savedOrder.getId()), any(EventPayload.class));
        verify(orderProcessedEventDbRepository, times(1)).save(any(OrderProcessedEvent.class));

        assertThat(savedOrder.getOrderType()).isEqualTo(LIMIT);
        assertThat(savedOrder.getPrice()).isEqualTo(valueOf(1000));
        assertThat(savedOrder.getQuantity()).isEqualTo(valueOf(5));
        assertThat(savedOrder.getCoin().getSymbol()).isEqualTo("BTC");
    }

    @Test
    @DisplayName("중복 이벤트가 감지되어 이벤트가 처리되지 않는다.")
    void handleDuplicateEvent() {
        // given
        when(orderProcessedEventRepository.setIfAbsent(event.getEventId())).thenReturn(false);

        // when
        orderEventService.handleEvent(event);

        // then
        verify(orderProcessedEventRepository, times(1)).setIfAbsent(event.getEventId());
        verifyNoInteractions(timeProvider, coinService, userService, orderRepository, tradeEventSender, orderProcessedEventDbRepository);
    }

    @Test
    @DisplayName("코인 서비스 호출 중 예외 발생 시 롤백 및 런타임 예외가 발생한다.")
    void handleEvent_CoinServiceFailure_RollbackAndThrowsException() {
        // given
        when(orderProcessedEventRepository.setIfAbsent(event.getEventId())).thenReturn(true);
        when(coinService.getCoinOrThrow(payload.getSymbol())).thenThrow(new RuntimeException("Coin not found"));

        // when then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> orderEventService.handleEvent(event));

        assertTrue(thrown.getMessage().contains("[OrderEventService.handleEvent] Failed to process order creation for eventId"));
        verify(orderProcessedEventRepository, times(1)).setIfAbsent(event.getEventId());
        verify(orderProcessedEventRepository, times(1)).delete(null);
        verify(timeProvider, times(1)).now();
        verify(coinService, times(1)).getCoinOrThrow(payload.getSymbol());
        verifyNoInteractions(userService, orderRepository, tradeEventSender, orderProcessedEventDbRepository);
    }
}