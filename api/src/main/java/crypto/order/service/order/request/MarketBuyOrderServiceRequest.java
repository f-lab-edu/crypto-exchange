package crypto.order.service.order.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class MarketBuyOrderServiceRequest {

    private String symbol;
    private BigDecimal totalPrice;

    @Builder
    public MarketBuyOrderServiceRequest(String symbol, BigDecimal totalPrice) {
        this.symbol = symbol;
        this.totalPrice = totalPrice;
    }
}
