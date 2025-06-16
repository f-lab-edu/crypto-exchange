package crypto.trade;

import crypto.event.Event;
import crypto.trade.eventhandler.EventHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class TradeService {
    private final List<EventHandler> eventHandlers;

    public void handleEvent(Event event) {
        EventHandler eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }
        eventHandler.handle(event);
    }

    private EventHandler findEventHandler(Event event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }
}
