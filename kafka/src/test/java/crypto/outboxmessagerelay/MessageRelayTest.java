package crypto.outboxmessagerelay;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.payload.UnifiedEventPayload;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static crypto.event.EventType.*;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@Transactional
@ActiveProfiles("test")
@SpringBootTest(classes = crypto.AppApiApplication.class)
@EmbeddedKafka(partitions = 1, topics = {"crypto-order"})
class MessageRelayTest {

    @Autowired
    private OutboxEventPublisher outboxEventPublisher;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        consumer.subscribe(Collections.singletonList("crypto-order"));
        consumer.poll(Duration.ZERO);
    }

    @DisplayName("이벤트 발행 후 커밋 이전에 outBox 데이터를 생성한다.")
    @Test
    public void checkCreateOutboxBeforeCommit() {
        // given
        UnifiedEventPayload payload = UnifiedEventPayload.builder()
                .orderId(1L)
                .coinId(1L)
                .price(valueOf(1000))
                .build();

        // when
        outboxEventPublisher.publish(LIMIT_BUY_ORDER_CREATE, payload, 3L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        List<Outbox> outboxes = outboxRepository.findAll();
        assertThat(outboxes.size()).isEqualTo(1);

        Outbox outbox = outboxes.getFirst();
        assertThat(outbox.getEventType()).isEqualTo(LIMIT_BUY_ORDER_CREATE);
    }

    @DisplayName("이벤트 발행 후 커밋 이후에 메세지가 정상적으로 전송되고 outBox 데이터가 삭제된다.")
    @Test
    public void checkSendMessageToKafkaAndDeleteOutbox() {
        // given
        UnifiedEventPayload payload = UnifiedEventPayload.builder()
                .orderId(1L)
                .coinId(1L)
                .price(valueOf(1000))
                .build();

        // when
        outboxEventPublisher.publish(LIMIT_BUY_ORDER_CREATE, payload, 3L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        final ConsumerRecords<String, String>[] capturedRecords = new ConsumerRecords[1];

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    ConsumerRecords<String, String> polledRecords = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(1000), 1);

                    if (polledRecords.count() >= 1) {
                        capturedRecords[0] = polledRecords;
                        return true;
                    }
                    return false;
                });

        assertThat(capturedRecords[0]).isNotNull();
        assertThat(capturedRecords[0].count()).isEqualTo(1);

        ConsumerRecord<String, String> receivedRecord = capturedRecords[0].iterator().next();
        assertThat(receivedRecord.topic()).isEqualTo("crypto-order");
        assertThat(receivedRecord.key()).isEqualTo(String.valueOf(0));

        Event event = DataSerializer.deserialize(receivedRecord.value(), Event.class);
        assertThat(event.getType()).isEqualTo(LIMIT_BUY_ORDER_CREATE);
        assertThat(event.getPayload().getOrderId()).isEqualTo(1L);
        assertThat(event.getPayload().getCoinId()).isEqualTo(1L);
        assertThat(event.getPayload().getPrice()).isEqualTo(valueOf(1000));

        List<Outbox> outboxes = outboxRepository.findAll();
        assertThat(outboxes.size()).isEqualTo(0);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }
}

