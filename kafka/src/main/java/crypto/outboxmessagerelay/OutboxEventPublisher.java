package crypto.outboxmessagerelay;

import crypto.event.Event;
import crypto.event.EventType;

import crypto.event.payload.UnifiedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(EventType type, UnifiedEventPayload payload, Long shardKey) {
        Outbox outbox = Outbox.create(
                type,
                Event.of(
                        UUID.randomUUID().toString(), type, payload
                ).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
