package crypto.trade.service;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.*;
import crypto.trade.eventhandler.EventHandler;
import crypto.trade.eventhandler.exception.TradeNotFoundException;
import crypto.trade.eventhandler.exception.TradeOrderNotFoundException;
import crypto.trade.repository.TradeOrderRepository;
import crypto.trade.repository.TradeProcessedEventDbRepository;
import crypto.trade.repository.TradeProcessedEventRepository;
import crypto.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static crypto.event.EventType.LIMIT_BUY_ORDER_TRADE;
import static crypto.event.EventType.LIMIT_SELL_ORDER_TRADE;
import static crypto.trade.entity.TradeOrderStatus.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class TradeEventService {

    private final List<EventHandler> eventHandlers;
    private final TradeRepository tradeRepository;
    private final TradeProcessedEventRepository tradeProcessedEventRepository;
    private final TradeProcessedEventDbRepository tradeProcessedEventDbRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();
        Boolean isNewEvent = tradeProcessedEventRepository.setIfAbsent(eventId);

        if (!isNewEvent) {
            log.warn("[TradeEventService.handleEvent] Duplicate event detected, skipping processing. eventId={}", eventId);
            return;
        }

        try {
            EventPayload payload = event.getPayload();
            EventHandler eventHandler = findEventHandler(event);

            if (eventHandler != null) {

                if (event.getType() == LIMIT_BUY_ORDER_TRADE || event.getType() == LIMIT_SELL_ORDER_TRADE) {
                    TradeOrder newOrder = tradeOrderRepository.save(
                            TradeOrder.create(payload.getOrderId(), payload.getUserId(), payload.getSymbol(), payload.getPrice(),
                                    payload.getQuantity(), TradeOrderSide.valueOf(payload.getOrderSide()), timeProvider.now()));

                    eventHandler.handle(event, newOrder);
                } else {
                    eventHandler.handle(event, null);
                }

                tradeProcessedEventDbRepository.save(new TradeProcessedEvent(eventId));
                log.info("[TradeEventService.handleEvent] Event processed successfully. eventId={}", eventId);

            } else {
                throw new IllegalArgumentException("[TradeEventService.handleEvent] Unknown event type received: " + event.getType());
            }

        } catch (Exception e) {
            tradeProcessedEventRepository.delete(tradeProcessedEventRepository.generateKey(eventId));
            throw new RuntimeException("[TradeEventService.handleEvent] Failed to process trade creation for eventId: " + eventId, e);
        }
    }

    @Transactional
    public void handleFailEvent(Event event) {
        String eventId = event.getEventId();
        Boolean isNewEvent = tradeProcessedEventRepository.setIfAbsent(eventId);

        if (!isNewEvent) {
            log.warn("[TradeEventService.handleFailEvent] Duplicate event detected, skipping processing. eventId={}", eventId);
            return;
        }

        try {
            EventPayload payload = event.getPayload();
            TradeOrder makerTradeOrder = tradeOrderRepository.findById(payload.getMakerOrderId())
                    .orElseThrow(TradeOrderNotFoundException::new);
            TradeOrder takerTradeOrder = tradeOrderRepository.findById(payload.getMakerOrderId())
                    .orElseThrow(TradeOrderNotFoundException::new);

            makerTradeOrder.handleOrderStatus(CANCELLED);
            tradeOrderRepository.save(makerTradeOrder);

            takerTradeOrder.cancelQuantity(payload.getMatchedQuantity());

            if (takerTradeOrder.getOrderStatus() == FILLED) {
                takerTradeOrder.handleOrderStatus(OPEN);
            }

            Trade trade = tradeRepository.findById(payload.getTradeId())
                    .orElseThrow(TradeNotFoundException::new);

            trade.markDeleted(timeProvider.now());

            tradeProcessedEventDbRepository.save(new TradeProcessedEvent(eventId));
            log.info("[TradeEventService.handleFailEvent] TradeOrder cancellation successfully: {}", makerTradeOrder);

        } catch (Exception e) {
            tradeProcessedEventRepository.delete(tradeProcessedEventRepository.generateKey(eventId));
            throw new RuntimeException("[TradeEventService.handleFailEvent] Failed to process tradeOrder cancellation for eventId: " + eventId, e);
        }
    }

    private EventHandler findEventHandler(Event event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }
}
