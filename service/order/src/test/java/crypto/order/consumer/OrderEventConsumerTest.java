package crypto.order.consumer;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.order.service.order.OrderEventService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.CompletableFuture;

import static crypto.event.EventType.*;
import static crypto.event.EventType.Topic.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private DataSerializer dataSerializer;

    @Mock
    private OrderEventService orderEventService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    private String message;
    private EventPayload payload;
    private Event event;


    @BeforeEach
    void setUp() {
      message = "{\"eventId\":\"a1b2c3d4-e5f6-7890-1234-567890ab1def\"," +
              "\"type\":\"LIMIT_BUY_ORDER_CREATE\"," +
              "\"payload\":{" +
              "\"userId\":42," +
              "\"symbol\":\"BTC\"," +
              "}}";

      payload = EventPayload.builder()
                .userId(42L)
                .symbol("BTC")
                .build();

      event = Event.of("a1b2c3d4-e5f6-7890-1234-567890ab1def", LIMIT_BUY_ORDER_CREATE, payload);
    }

    @DisplayName("유효한 메시지가 수신되면 이벤트를 처리하고 acknowledge 한다")
    @Test
    void listenSuccess() {
        // given
        when(dataSerializer.deserialize(message, Event.class)).thenReturn(event);
        doNothing().when(orderEventService).handleEvent(event);

        // when
        orderEventConsumer.listen(message, ack);

        // then
        verify(dataSerializer, times(1)).deserialize(message, Event.class);
        verify(orderEventService, times(1)).handleEvent(event);
        verify(kafkaTemplate, never()).send(eq(CRYPTO_ORDER_DLQ), anyString(), anyString());
    }

    @DisplayName("메시지 역직렬화 실패시 DLQ로 보내고 acknowledge 한다")
    @Test
    void listenDeserializeFailure() {
        // given
        when(dataSerializer.deserialize(message, Event.class)).thenReturn(null);
        when(kafkaTemplate.send(eq(CRYPTO_ORDER_DLQ), eq(null)))
                .thenReturn(mock(CompletableFuture.class));

        // when
        orderEventConsumer.listen(message, ack);

        // then
        verify(dataSerializer, times(1)).deserialize(message, Event.class);
        verify(orderEventService, never()).handleEvent(any(Event.class));
        verify(kafkaTemplate, times(1)).send(eq(CRYPTO_ORDER_DLQ), eq(null));
        verify(ack, times(1)).acknowledge();
    }

    @DisplayName("이벤트 처리 중 예외 발생시 DLQ로 보내고 acknowledge 한다")
    @Test
    void listenHandleEventFailure() {
        // given
        when(dataSerializer.deserialize(message, Event.class)).thenReturn(event);
        doThrow(new RuntimeException("Test Exception")).when(orderEventService).handleEvent(event);

        when(kafkaTemplate.send(eq(CRYPTO_ORDER_DLQ), eq(null)))
                .thenReturn(mock(CompletableFuture.class));

        // when
        orderEventConsumer.listen(message, ack);

        // then
        verify(dataSerializer, times(1)).deserialize(message, Event.class);
        verify(orderEventService, times(1)).handleEvent(event);
        verify(kafkaTemplate, times(1)).send(eq(CRYPTO_ORDER_DLQ), eq(null));
        verify(ack, times(1)).acknowledge();
    }
}