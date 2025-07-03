package crypto.order.consumer;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.order.service.order.OrderEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static crypto.event.EventType.FAIL_ORDER_EVENT;
import static crypto.event.EventType.Topic;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final DataSerializer dataSerializer;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderEventService orderEventService;

    @KafkaListener(topics = Topic.CRYPTO_ORDER, groupId = "crypto-order", id = "orderListener")
    public void listen(String message, Acknowledgment ack) {
        log.info("[OrderEventConsumer.listen] received message={}", message);
        Event event = dataSerializer.deserialize(message, Event.class);

        if (event == null) {
            log.error("[OrderEventConsumer.listen] Failed to parse event from message: message={}", message);
            sendToDeadLetterQueue(message, "EVENT_IS_NULL_AFTER_PARSING");
            ack.acknowledge();
            return;
        }

        try {
            orderEventService.handleEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[OrderEventConsumer.listen] Error processing order event: event={}, message={}, error={}", event, message, e.getMessage(), e);
            sendToDeadLetterQueue(message, "ERROR_PROCESSING_ORDER_EVENT");
            ack.acknowledge();
        }
    }

    public void sendToDeadLetterQueue(String originalMessage, String failMessage) {
        String dlqTopic = Topic.CRYPTO_ORDER_DLQ;

        EventPayload payload = EventPayload.builder()
                .originalMessage(originalMessage)
                .failMessage(failMessage)
                .build();

        String json = dataSerializer.serialize(Event.of(
                UUID.randomUUID().toString(), FAIL_ORDER_EVENT, payload
        ));

        kafkaTemplate.send(dlqTopic, json)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[OrderEventConsumer.sendToDeadLetterQueue] Successfully sent message to DLQ. topic={}, message={}", dlqTopic, originalMessage);
                    } else {
                        log.error("[OrderEventConsumer.sendToDeadLetterQueue] Failed to send message to DLQ. topic={}, message={}, error={}", dlqTopic, originalMessage, ex.getMessage(), ex);
                    }
                });
    }
}
