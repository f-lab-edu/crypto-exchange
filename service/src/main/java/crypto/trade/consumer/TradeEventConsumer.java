package crypto.trade.consumer;

import crypto.event.Event;
import crypto.event.EventPayload;
import crypto.event.EventType;
import crypto.trade.TradeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {
    private final TradeService tradeService;

    @KafkaListener(topics = {
            EventType.Topic.CRYPTO_ORDER,
            EventType.Topic.CRYPTO_TRADE
    }, groupId = "crypto-order")
    public void listen(String message, Acknowledgment ack) {
        log.info("[TradeEventConsumer.listen] received message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            tradeService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
