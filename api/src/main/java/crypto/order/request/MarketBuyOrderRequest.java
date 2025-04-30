package crypto.order.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
@NoArgsConstructor
public class MarketBuyOrderRequest extends OrderRequest {

    private Integer totalPrice;
}
