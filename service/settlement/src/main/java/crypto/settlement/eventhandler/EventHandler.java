package crypto.settlement.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;


public interface EventHandler {
    void handle(Event event);

    EventType getSupportedEventType();

    default boolean supports(Event event) {
        return getSupportedEventType() == event.getType();
    };
}