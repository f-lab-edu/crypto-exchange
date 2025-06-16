package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventPayload;


public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}