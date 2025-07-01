package crypto.order.consumer;

import crypto.common.time.TimeProvider;
import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import crypto.order.entity.user.User;
import crypto.order.repository.order.OrderRepository;
import crypto.order.service.coin.CoinService;
import crypto.order.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import static crypto.event.EventType.*;
import static crypto.event.EventType.FAIL_ORDER_EVENT;
import static crypto.event.EventType.Topic;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final DataSerializer dataSerializer;
    private final TimeProvider timeProvider;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final CoinService coinService;
    private final UserService userService;

    @KafkaListener(topics = Topic.CRYPTO_ORDER, groupId = "crypto-order", id = "orderListener")
    public void listen(String message, Acknowledgment ack) {
        log.info("[OrderEventConsumer.listen] received message={}", message);
        Event event = dataSerializer.deserialize(message, Event.class);

        if (event != null) {
            EventPayload payload = event.getPayload();
            LocalDateTime registeredDateTime = timeProvider.now();
            Coin coin = coinService.getCoinOrThrow(payload.getSymbol());
            User user = userService.getUserOrThrow(payload.getUserId());
            Order order = null;

            if (event.getType() == LIMIT_BUY_ORDER_CREATE || event.getType() == LIMIT_SELL_ORDER_CREATE) {
                order = Order.createLimitOrder(
                        payload.getPrice(),
                        payload.getQuantity(),
                        OrderSide.valueOf(payload.getOrderSide()),
                        coin,
                        user,
                        registeredDateTime
                );
            } else if (event.getType() == MARKET_BUY_ORDER_CREATE) {
                order = Order.createMarketBuyOrder(
                        payload.getMarketTotalPrice(),
                        coin,
                        user,
                        registeredDateTime
                );
            } else if (event.getType() == MARKET_SELL_ORDER_CREATE) {
                order = Order.createMarketSellOrder(
                        payload.getMarketTotalQuantity(),
                        coin,
                        user,
                        registeredDateTime
                );
            } else {
                log.error("[OrderEventConsumer.listen] Unknown event type received: {}", event.getType());
                sendToDeadLetterQueue(message, "UNKNOWN_EVENT_TYPE");
                ack.acknowledge();
                return;
            }

            if (order != null) {
                try {
                    orderRepository.save(order);
                    log.info("[OrderEventConsumer.listen] Order saved successfully: {}", order);
                } catch (Exception e) {
                    log.error("[OrderEventConsumer.listen] Error processing order event: event={}, message={}, error={}", event, message, e.getMessage(), e);
                    sendToDeadLetterQueue(message, "ERROR_PROCESSING_TRADE_EVENT");
                    ack.acknowledge();
                    return;
                }
            }

        } else {
            log.error("[OrderEventConsumer.listen] Failed to parse event from message: message={}", message);
            sendToDeadLetterQueue(message, "EVENT_IS_NULL_AFTER_PARSING");
            ack.acknowledge();
            return;
        }
        ack.acknowledge();
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
                        log.info("[TradeEventConsumer.sendToDeadLetterQueue] Successfully sent message to DLQ. topic={}, message={}", dlqTopic, originalMessage);
                    } else {
                        log.error("[TradeEventConsumer.sendToDeadLetterQueue] Failed to send message to DLQ. topic={}, message={}, error={}", dlqTopic, originalMessage, ex.getMessage(), ex);
                    }
                });
    }
}
