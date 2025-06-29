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
public class EventPayload {
    private Long orderId;
    private Long userId;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal marketTotalQuantity;
    private BigDecimal marketTotalPrice;
    private String orderSide;

    private Long takerId;
    private Long makerId;
    private BigDecimal takerTotalUsed;
    private BigDecimal makerTotalUsed;
    private BigDecimal matchedQuantity;
    private BigDecimal totalRemainPrice;

    private String originalMessage;
    private String failMessage;
}
