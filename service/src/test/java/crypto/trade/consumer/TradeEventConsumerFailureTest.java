package crypto.trade.consumer;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.trade.TradeService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringBootTest(classes = crypto.AppApiApplication.class)
@EmbeddedKafka(partitions = 1, topics = {"crypto-limit-order", "crypto-market-order", "crypto-order-dlq"})
public class TradeEventConsumerFailureTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private TradeEventConsumer tradeEventConsumer;

    @MockitoBean
    private TradeService tradeService;

    private BlockingQueue<ConsumerRecord<String, String>> dlqRecords;

    @KafkaListener(topics = "crypto-order-dlq", groupId = "dlq-group", autoStartup = "true")
    void listenDlq(ConsumerRecord<String, String> record) {
        this.dlqRecords.add(record);
    }

    @BeforeEach
    void setUp() {
        dlqRecords = new LinkedBlockingQueue<>();
    }

    @Test
    void testListen_InvalidEvent_SendsToDeadLetterQueue() throws Exception {
        // given
        String invalidMessage = "invalid_json_format";

        MessageListenerContainer container = registry.getListenerContainer("orderListener");
        ContainerTestUtils.waitForAssignment(container, 2);

        // when
        kafkaTemplate.send(EventType.Topic.CRYPTO_MARKET_ORDER, invalidMessage).get(5, TimeUnit.SECONDS);

        // then
        verify(tradeService, times(0)).handleEvent(any(Event.class));

        ConsumerRecord<String, String> receivedDlqRecord = dlqRecords.poll(5, TimeUnit.SECONDS);
        String dlqMessageValue = receivedDlqRecord.value();

        assertThat(dlqMessageValue, containsString("EVENT_IS_NULL_AFTER_PARSING"));
    }
}
