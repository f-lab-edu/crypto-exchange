package crypto.trade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Getter
@Entity
@NoArgsConstructor
public class TradeProcessedEvent {
    @Id @Column(unique = true)
    private String eventId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;

    public TradeProcessedEvent(String eventId) {
        this.eventId = eventId;
    }
}
