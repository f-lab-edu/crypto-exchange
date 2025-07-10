package crypto.event.eventsender;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.EventPayload;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TradeEventSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DataSerializer dataSerializer;

    @Async("publishEventExecutor")
    public void send(EventType type, Long key, EventPayload payload) {
        kafkaTemplate.send(
            type.getTopic(),
            String.valueOf(key),
            dataSerializer.serialize(Event.of(
                    UUID.randomUUID().toString(),
                    type,
                    payload
            ))
        );
    }
}
