package crypto.order.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Getter
@SuperBuilder
@NoArgsConstructor
public class MarketSellOrderRequest extends OrderRequest {

    private BigDecimal totalAmount;
}
