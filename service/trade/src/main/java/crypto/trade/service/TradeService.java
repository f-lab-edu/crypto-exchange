package crypto.trade.service;

import crypto.event.Event;
import crypto.trade.entity.ProcessedEvent;
import crypto.trade.eventhandler.EventHandler;
import crypto.trade.repository.ProcessedEventRepository;

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
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();

        if (processedEventRepository.existsById(Long.valueOf(eventId))) {
            log.info("[TradeService.handleEvent] Already processed event. eventId={}", eventId);
            return;
        }

        EventHandler eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }
        eventHandler.handle(event);

        processedEventRepository.save(new ProcessedEvent(eventId));
    }

    private EventHandler findEventHandler(Event event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }
}
