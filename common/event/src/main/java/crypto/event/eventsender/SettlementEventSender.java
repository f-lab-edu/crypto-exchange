package crypto.event.eventsender;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.EventPayload;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static crypto.event.EventType.FAIL_SETTLEMENT_EVENT;
import static crypto.event.EventType.FAIL_TRADE_EVENT;


@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DataSerializer dataSerializer;

    @Async("publishEventExecutor")
    public void send(EventType type, Long key, EventPayload payload) {
        String message = dataSerializer.serialize(Event.of(
                UUID.randomUUID().toString(),
                type,
                payload
        ));

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                type.getTopic(), String.valueOf(key), message
        );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[SettlementEventSender.send] Unable to send message=[{}] due to an error", message, ex);
                sendToDeadLetterQueue(message, "ERROR_SENDING_ORDER_EVENT");
            } else {
                log.info("[SettlementEventSender.send] Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
            }
        });
    }

    public void sendToDeadLetterQueue(String originalMessage, String failMessage) {
        String dlqTopic = EventType.Topic.CRYPTO_SETTLEMENT_DLQ;

        EventPayload payload = EventPayload.builder()
                .originalMessage(originalMessage)
                .failMessage(failMessage)
                .build();

        String json = dataSerializer.serialize(Event.of(
                UUID.randomUUID().toString(), FAIL_SETTLEMENT_EVENT, payload
        ));

        kafkaTemplate.send(dlqTopic, json)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[SettlementEventSender.sendToDeadLetterQueue] Successfully sent message to DLQ. topic={}, message={}", dlqTopic, originalMessage);
                    } else {
                        log.error("[SettlementEventSender.sendToDeadLetterQueue] Failed to send message to DLQ. topic={}, message={}, error={}", dlqTopic, originalMessage, ex.getMessage(), ex);
                    }
                });
    }
}
