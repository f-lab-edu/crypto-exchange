package crypto.event;

import crypto.dataserializer.DataSerializer;
import crypto.event.payload.EventPayload;

import lombok.Getter;


@Getter
public class Event {
    private String eventId;
    private EventType type;
    private EventPayload payload;

    public static Event of(String eventId, EventType type, EventPayload payload) {
        Event event = new Event();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    public String toJson() {
        return DataSerializer.serialize(this);
    }

    public static Event fromJson(String json) {
        return DataSerializer.deserialize(json, Event.class);
    }
}
