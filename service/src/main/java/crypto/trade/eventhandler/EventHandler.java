package crypto.trade.eventhandler;

import crypto.event.Event;


public interface EventHandler {
    void handle(Event event);
    boolean supports(Event event);
}