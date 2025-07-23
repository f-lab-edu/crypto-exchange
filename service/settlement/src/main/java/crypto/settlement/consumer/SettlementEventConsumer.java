package crypto.settlement.consumer;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.eventsender.TradeEventSender;
import crypto.event.payload.EventPayload;
import crypto.settlement.service.SettlementEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static crypto.event.EventType.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventConsumer {

    private final DataSerializer dataSerializer;
    private final TradeEventSender tradeEventSender;
    private final SettlementEventService settlementEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = Topic.CRYPTO_SETTLEMENT, groupId = "crypto-trade", id = "settlementListener")
    public void listen(String message, Acknowledgment ack) {
        log.info("[SettlementEventConsumer.listen] received message={}", message);
        Event event = dataSerializer.deserialize(message, Event.class);

        if (event == null) {
            log.error("[SettlementEventConsumer.listen] Failed to parse event from message: message={}", message);
            sendToDeadLetterQueue(message, "EVENT_IS_NULL_AFTER_PARSING");
            ack.acknowledge();
            return;
        }

        try {
            settlementEventService.handleEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[SettlementEventConsumer.listen] Error processing settlement event: event={}, message={}, error={}", event, message, e.getMessage(), e);

            try {
                Long orderId = event.getPayload().getOrderId();
                Long tradeId = event.getPayload().getTradeId();
                BigDecimal matchedQuantity = event.getPayload().getMatchedQuantity();
                EventPayload failurePayload = EventPayload.builder()
                        .orderId(orderId)
                        .tradeId(tradeId)
                        .matchedQuantity(matchedQuantity)
                        .failMessage("Settlement execution failed: " + e.getMessage())
                        .build();
                tradeEventSender.sendFailEvent(TRADE_CANCEL_EVENT, orderId, failurePayload);
                log.info("[SettlementEventConsumer.listen] Sent settlement execution failure event for orderId: {}", orderId);

            } catch (Exception sendEx) {
                log.error("[SettlementEventConsumer.listen] CRITICAL: Failed to send failure event to order application. error={}", sendEx.getMessage(), sendEx);
            }
            sendToDeadLetterQueue(message, "ERROR_PROCESSING_SETTLEMENT_EVENT");
            ack.acknowledge();
        }
    }
    public void sendToDeadLetterQueue(String originalMessage, String failMessage) {
        String dlqTopic = Topic.CRYPTO_SETTLEMENT_DLQ;

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
                        log.info("[SettlementEventConsumer.sendToDeadLetterQueue] Successfully sent message to DLQ. topic={}, message={}", dlqTopic, originalMessage);
                    } else {
                        log.error("[SettlementEventConsumer.sendToDeadLetterQueue] Failed to send message to DLQ. topic={}, message={}, error={}", dlqTopic, originalMessage, ex.getMessage(), ex);
                    }
                });
    }
}
