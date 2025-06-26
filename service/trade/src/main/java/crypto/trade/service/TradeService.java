package crypto.trade.service;

import crypto.event.Event;
import crypto.trade.entity.TradeProcessedEvent;
import crypto.trade.eventhandler.EventHandler;
import crypto.trade.repository.TradeProcessedEventDbRepository;
import crypto.trade.repository.TradeProcessedEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class TradeService {
    private final List<EventHandler> eventHandlers;
    private final TradeProcessedEventRepository tradeProcessedEventRepository;
    private final TradeProcessedEventDbRepository tradeProcessedEventDbRepository;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();
        String key = tradeProcessedEventRepository.generateKey(eventId);

        Boolean isNewEvent = tradeProcessedEventRepository.setIfAbsent(eventId);

        if (Boolean.FALSE.equals(isNewEvent)) {
            log.info("[TradeService.handleEvent] Already processed or is being processed. eventId={}", eventId);
            return;
        }

        try {
            EventHandler eventHandler = findEventHandler(event);
            if (eventHandler == null) {
                log.warn("[TradeService.handleEvent] No handler found for event. eventId={}", eventId);
                tradeProcessedEventRepository.delete(key);
                return;
            }

            eventHandler.handle(event);

            tradeProcessedEventDbRepository.save(new TradeProcessedEvent(eventId));
            log.info("[TradeService.handleEvent] Event processed successfully. eventId={}", eventId);

        } catch (Exception e) {
            log.error("[TradeService.handleEvent] Failed to handle event due to an exception. eventId={}", eventId, e);
            tradeProcessedEventRepository.delete(key);

            throw new RuntimeException("Event handling failed", e);
        }
    }

    private EventHandler findEventHandler(Event event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }
}
