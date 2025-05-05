package crypto.order.request;

import crypto.order.OrderSide;
import crypto.order.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class LimitBuyOrderRequest {

    private String symbol;
    private OrderType orderType;
    private OrderSide orderSide;
    private BigDecimal price;
    private BigDecimal quantity;

    @Builder
    public LimitBuyOrderRequest(String symbol, OrderType orderType, OrderSide orderSide, BigDecimal price, BigDecimal quantity) {
        this.symbol = symbol;
        this.orderType = orderType;
        this.orderSide = orderSide;
        this.price = price;
        this.quantity = quantity;
    }
}
