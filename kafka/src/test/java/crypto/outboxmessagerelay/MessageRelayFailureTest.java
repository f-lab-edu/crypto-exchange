package crypto.outboxmessagerelay;

import crypto.event.payload.UnifiedEventPayload;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static crypto.event.EventType.LIMIT_SELL_ORDER_CREATE;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@Transactional
@ActiveProfiles("test")
@SpringBootTest(classes = crypto.AppApiApplication.class)
public class MessageRelayFailureTest {

    @MockitoBean
    private KafkaTemplate<String, String> mockKafkaTemplate;

    @Autowired
    private OutboxEventPublisher outboxEventPublisher;

    @Autowired
    private OutboxRepository outboxRepository;

    @DisplayName("이벤트 발행 후 메세지 전송이 실패한 경우 outBox 데이터가 삭제되지 않는다.")
    @Test
    public void checkSendMessageFailToKafkaAndOutboxDataRemain() {
        // given
        doThrow(new KafkaException("Kafka send failure"))
                .when(mockKafkaTemplate)
                .send(anyString(), anyString(), anyString());

        UnifiedEventPayload payload = UnifiedEventPayload.builder()
                .orderId(1L)
                .coinId(1L)
                .price(valueOf(1000))
                .build();

        // when
        outboxEventPublisher.publish(LIMIT_SELL_ORDER_CREATE, payload, 3L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Outbox> outboxes = outboxRepository.findAll();
                    assertThat(outboxes).hasSize(1);

                    Outbox remainingOutbox = outboxes.getFirst();
                    assertThat(remainingOutbox.getEventType()).isEqualTo(LIMIT_SELL_ORDER_CREATE);
                });

        verify(mockKafkaTemplate, atLeastOnce()).send(anyString(), anyString(), anyString());
    }
}
