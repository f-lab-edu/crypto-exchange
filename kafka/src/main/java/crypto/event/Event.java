package crypto.event;

import crypto.dataserializer.DataSerializer;
import crypto.event.payload.UnifiedEventPayload;
import lombok.Getter;

@Getter
public class Event {
    private String eventId;
    private EventType type;
    private UnifiedEventPayload payload;

    public static Event of(String eventId, EventType type, UnifiedEventPayload payload) {
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
