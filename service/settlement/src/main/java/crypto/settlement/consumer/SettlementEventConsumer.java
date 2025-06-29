package crypto.settlement.consumer;

import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.settlement.service.SettlementService;
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
public class SettlementEventConsumer {

    private final SettlementService settlementService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = Topic.CRYPTO_TRADE, groupId = "crypto-trade", id = "tradeListener")
    public void listen(String message, Acknowledgment ack) {
        log.info("[SettlementEventConsumer.listen] received message={}", message);
        Event event = Event.fromJson(message);

        if (event != null) {
            EventPayload payload = event.getPayload();
            try {
                settlementService.handleEvent(event);
            } catch (Exception e) {
                log.error("[SettlementEventConsumer.listen] Error processing settlement event: event={}, message={}, error={}", event, message, e.getMessage(), e);
                sendToDeadLetterQueue(message, "ERROR_PROCESSING_SETTLEMENT_EVENT");
            }
        } else {
            log.error("[SettlementEventConsumer.listen] Failed to parse event from message: message={}", message);
            sendToDeadLetterQueue(message, "EVENT_IS_NULL_AFTER_PARSING");
        }
        ack.acknowledge();
    }
    public void sendToDeadLetterQueue(String originalMessage, String failMessage) {
        String dlqTopic = Topic.CRYPTO_TRADE_DLQ;

        EventPayload payload = EventPayload.builder()
                .originalMessage(originalMessage)
                .failMessage(failMessage)
                .build();

        String json = Event.of(
                UUID.randomUUID().toString(), FAIL_TRADE_EVENT, payload
        ).toJson();

        kafkaTemplate.send(dlqTopic, json)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[SettlementEventConsumer.sendToDeadLetterQueue] Successfully sent message to DLQ. topic={}, message={}", dlqTopic, originalMessage);
                    } else {
                        log.error("[SettlementEventConsumer.sendToDeadLetterQueue] Failed to send message to DLQ. topic={}, message={}, error={}", dlqTopic, originalMessage, ex.getMessage(), ex);
                    }
                });
    }
}
