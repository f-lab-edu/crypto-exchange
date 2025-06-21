package crypto.event.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedEventPayload {
    private Long orderId;
    private Long coinId;
    private BigDecimal price;
    private BigDecimal marketTotalQuantity;
    private BigDecimal marketTotalPrice;
    private String originalMessage;
    private String failMessage;
}
