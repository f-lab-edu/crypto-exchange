package crypto.order.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class MarketBuyOrderRequest {

    private String symbol;
    private BigDecimal totalPrice;

    @Builder
    public MarketBuyOrderRequest(String symbol, BigDecimal totalPrice) {
        this.symbol = symbol;
        this.totalPrice = totalPrice;
    }

    public MarketBuyOrderServiceRequest toServiceRequest() {
        return MarketBuyOrderServiceRequest.builder()
                .symbol(symbol)
                .totalPrice(totalPrice)
                .build();
    }
}
