package crypto.outboxmessagerelay;

import crypto.event.EventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {

    @Id @Column(name = "outbox_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Long shardKey;
    private String payload;
    private LocalDateTime createdAt;

    public static Outbox create(EventType eventType, String payload, Long shardKey) {
        Outbox outbox = new Outbox();
        outbox.eventType = eventType;
        outbox.shardKey = shardKey;
        outbox.payload = payload;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}
