package crypto.event;

import crypto.event.payload.EventPayload;

import lombok.Getter;


@Getter
public class TradeEvent {
    private EventType type;
    private Long key;
    private EventPayload payload;

    public static TradeEvent of(EventType type, Long key, EventPayload payload) {
        TradeEvent tradeEvent = new TradeEvent();
        tradeEvent.type = type;
        tradeEvent.key = key;
        tradeEvent.payload = payload;
        return tradeEvent;
    }
}
