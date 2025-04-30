package crypto.order.request;

import crypto.order.OrderSide;
import crypto.order.OrderType;

public abstract class OrderRequest {

    private String symbol;
    private OrderType orderType;
    private OrderSide orderSide;
}
