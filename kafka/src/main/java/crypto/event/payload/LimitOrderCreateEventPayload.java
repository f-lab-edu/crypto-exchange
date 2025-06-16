package crypto.event.payload;

import crypto.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitOrderCreateEventPayload implements EventPayload {
    private Long orderId;
    private Long coinId;
    private BigDecimal price;
}