package crypto.trade.service;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.TradeOrder;
import crypto.trade.entity.TradeOrderSide;
import crypto.trade.entity.TradeProcessedEvent;
import crypto.trade.eventhandler.EventHandler;
import crypto.trade.repository.TradeOrderRepository;
import crypto.trade.repository.TradeProcessedEventDbRepository;
import crypto.trade.repository.TradeProcessedEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static crypto.event.EventType.LIMIT_BUY_ORDER_TRADE;
import static crypto.event.EventType.LIMIT_SELL_ORDER_TRADE;


@Slf4j
@RequiredArgsConstructor
@Service
public class TradeEventService {

    private final List<EventHandler> eventHandlers;
    private final TradeProcessedEventRepository tradeProcessedEventRepository;
    private final TradeProcessedEventDbRepository tradeProcessedEventDbRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();
        Boolean isNewEvent = tradeProcessedEventRepository.setIfAbsent(eventId);

        if (!isNewEvent) {
            log.warn("[TradeService.handleEvent] Duplicate event detected, skipping processing. eventId={}", eventId);
            return;
        }

        try {
            EventPayload payload = event.getPayload();
            EventHandler eventHandler = findEventHandler(event);

            if (eventHandler != null) {

                if (event.getType() == LIMIT_BUY_ORDER_TRADE || event.getType() == LIMIT_SELL_ORDER_TRADE) {
                    tradeOrderRepository.save(
                            TradeOrder.create(payload.getOrderId(), payload.getUserId(), payload.getSymbol(), payload.getPrice(),
                                    payload.getQuantity(), TradeOrderSide.valueOf(payload.getOrderSide()), timeProvider.now()));
                }
                eventHandler.handle(event);

                tradeProcessedEventDbRepository.save(new TradeProcessedEvent(eventId));
                log.info("[TradeService.handleEvent] Event processed successfully. eventId={}", eventId);

            } else {
                throw new IllegalArgumentException("Unknown event type received: " + event.getType());
            }

        } catch (Exception e) {
            tradeProcessedEventRepository.delete(tradeProcessedEventRepository.generateKey(eventId));
            throw new RuntimeException("Failed to process trade creation for eventId: " + eventId, e);
        }
    }

    private EventHandler findEventHandler(Event event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }
}
