package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.trade.entity.TradeOrder;


public interface EventHandler {
    void handle(Event event, TradeOrder tradeOrder);

    EventType getSupportedEventType();

    default boolean supports(Event event) {
        return getSupportedEventType() == event.getType();
    };
}