package crypto.trade.consumer;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.trade.service.TradeEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static crypto.event.EventType.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {

    private final DataSerializer dataSerializer;
    private final TradeEventService tradeEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = Topic.CRYPTO_TRADE, groupId = "crypto-trade", id = "tradeListener")
    public void listen(String message, Acknowledgment ack) {
        log.info("[TradeEventConsumer.listen] received message={}", message);
        Event event = dataSerializer.deserialize(message, Event.class);

        if (event == null) {
            log.error("[TradeEventConsumer.listen] Failed to parse event from message: message={}", message);
            sendToDeadLetterQueue(message, "EVENT_IS_NULL_AFTER_PARSING");
            ack.acknowledge();
            return;
        }

        try {
            tradeEventService.handleEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[TradeEventConsumer.listen] Error processing trade event: event={}, message={}, error={}", event, message, e.getMessage(), e);
            sendToDeadLetterQueue(message, "ERROR_PROCESSING_TRADE_EVENT");
            ack.acknowledge();
        }
    }

    public void sendToDeadLetterQueue(String originalMessage, String failMessage) {
        String dlqTopic = Topic.CRYPTO_TRADE_DLQ;

        EventPayload payload = EventPayload.builder()
                .originalMessage(originalMessage)
                .failMessage(failMessage)
                .build();

        String json = dataSerializer.serialize(Event.of(
                UUID.randomUUID().toString(), FAIL_TRADE_EVENT, payload
        ));

        kafkaTemplate.send(dlqTopic, json)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[TradeEventConsumer.sendToDeadLetterQueue] Successfully sent message to DLQ. topic={}, message={}", dlqTopic, originalMessage);
                    } else {
                        log.error("[TradeEventConsumer.sendToDeadLetterQueue] Failed to send message to DLQ. topic={}, message={}, error={}", dlqTopic, originalMessage, ex.getMessage(), ex);
                    }
                });
    }
}
