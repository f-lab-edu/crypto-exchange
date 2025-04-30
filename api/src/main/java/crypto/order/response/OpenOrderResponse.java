package crypto.order.response;

import crypto.order.OrderSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OpenOrderResponse {

    private String orderId;
    private String symbol;
    private OrderSide side;
    private BigDecimal price;
    private BigDecimal requestQty;
    private BigDecimal remainQty;
    private LocalDateTime createdAt;
}
