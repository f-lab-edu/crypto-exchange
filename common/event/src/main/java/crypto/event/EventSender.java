package crypto.event;

import crypto.dataserializer.DataSerializer;
import crypto.event.payload.EventPayload;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class EventSender {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DataSerializer dataSerializer;

    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 필요한지 확인
    public void publish(EventType type, EventPayload payload) {
        kafkaTemplate.send(
                type.getTopic(),
                dataSerializer.serialize(Event.of(
                        UUID.randomUUID().toString(),
                        type,
                        payload
                ))
        );
    }
}
