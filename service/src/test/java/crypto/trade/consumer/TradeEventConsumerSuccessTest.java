package crypto.trade.consumer;

import crypto.event.Event;
import crypto.event.payload.UnifiedEventPayload;
import crypto.trade.TradeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static crypto.event.EventType.*;
import static java.math.BigDecimal.valueOf;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringBootTest(classes = crypto.AppApiApplication.class)
@EmbeddedKafka(partitions = 1, topics = {"crypto-limit-order", "crypto-market-order"})
class TradeEventConsumerSuccessTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private TradeEventConsumer tradeEventConsumer;

    @MockitoBean
    private TradeService tradeService;


    @DisplayName("이벤트가 발행되면 tradeEventConsumer 가 handleEvent 메서드를 실행한다.")
    @Test
    void checkHandlesEventSuccessfully() throws Exception {
        // given
        UnifiedEventPayload payload = UnifiedEventPayload.builder()
                .orderId(1L)
                .coinId(1L)
                .price(valueOf(1000))
                .build();

        String jsonEvent = Event.of("123", LIMIT_BUY_ORDER_CREATE, payload).toJson();

        MessageListenerContainer container = registry.getListenerContainer("orderListener");
        ContainerTestUtils.waitForAssignment(container, 2);

        // when
        kafkaTemplate.send(Topic.CRYPTO_LIMIT_ORDER, jsonEvent).get(10, TimeUnit.SECONDS);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(tradeService, times(1)).handleEvent(any(Event.class))
        );
    }
}