package crypto.trade.consumer;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.TradeOrder;
import crypto.trade.entity.TradeOrderSide;
import crypto.trade.repository.TradeOrderRepository;
import crypto.trade.service.TradeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static crypto.event.EventType.*;
import static crypto.event.EventType.FAIL_ORDER_EVENT;


@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {

    private final TradeService tradeService;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = Topic.CRYPTO_ORDER, groupId = "crypto-order", id = "orderListener")
    public void listen(String message, Acknowledgment ack) {
        log.info("[TradeEventConsumer.listen] received message={}", message);
        Event event = Event.fromJson(message);

        if (event != null) {
            EventPayload payload = event.getPayload();
            try {
                tradeOrderRepository.save(
                        TradeOrder.create(payload.getOrderId(), payload.getUserId(), payload.getSymbol(), payload.getPrice(),
                                payload.getQuantity(), TradeOrderSide.valueOf(payload.getOrderSide()), timeProvider.now())
                );
                tradeService.handleEvent(event);
            } catch (Exception e) {
                log.error("[TradeEventConsumer.listen] Error processing trade event: event={}, message={}, error={}", event, message, e.getMessage(), e);
                sendToDeadLetterQueue(message, "ERROR_PROCESSING_TRADE_EVENT");
            }
        } else {
            log.error("[TradeEventConsumer.listen] Failed to parse event from message: message={}", message);
            sendToDeadLetterQueue(message, "EVENT_IS_NULL_AFTER_PARSING");
        }
        ack.acknowledge();
    }

    public void sendToDeadLetterQueue(String originalMessage, String failMessage) {
        String dlqTopic = Topic.CRYPTO_ORDER_DLQ;

        EventPayload payload = EventPayload.builder()
                .originalMessage(originalMessage)
                .failMessage(failMessage)
                .build();

        String json = Event.of(
                UUID.randomUUID().toString(), FAIL_ORDER_EVENT, payload
        ).toJson();

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
