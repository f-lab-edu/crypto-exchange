package crypto.order.request;

import crypto.order.OrderSide;
import crypto.order.OrderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class OrderRequest {

    private String symbol;
    private OrderType orderType;
    private OrderSide orderSide;
}
