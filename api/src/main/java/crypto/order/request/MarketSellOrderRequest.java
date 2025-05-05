package crypto.order.request;

import crypto.order.OrderSide;
import crypto.order.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class MarketSellOrderRequest {

    private String symbol;
    private OrderType orderType;
    private OrderSide orderSide;
    private BigDecimal totalAmount;

    @Builder
    public MarketSellOrderRequest(String symbol, OrderType orderType, OrderSide orderSide, BigDecimal totalAmount) {
        this.symbol = symbol;
        this.orderType = orderType;
        this.orderSide = orderSide;
        this.totalAmount = totalAmount;
    }
}
