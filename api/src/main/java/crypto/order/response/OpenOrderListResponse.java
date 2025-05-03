package crypto.order.response;

import crypto.order.OrderSide;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder
public class OpenOrderListResponse {

    private String orderId;
    private String symbol;
    private OrderSide orderSide;
    private BigDecimal price;
    private BigDecimal requestQty;
    private BigDecimal remainQty;
    private LocalDateTime requestedAt;
}
