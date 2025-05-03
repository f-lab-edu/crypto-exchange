package crypto.order.response;


import crypto.order.OrderSide;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CompleteOrderListResponse {

    private String orderId;
    private String symbol;
    private OrderSide orderSide;
    private BigDecimal price;
    private BigDecimal amount;
}
