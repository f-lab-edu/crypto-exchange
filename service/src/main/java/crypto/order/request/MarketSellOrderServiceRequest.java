package crypto.order.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class MarketSellOrderServiceRequest {

    private String symbol;
    private BigDecimal totalAmount;

    @Builder
    public MarketSellOrderServiceRequest(String symbol, BigDecimal totalAmount) {
        this.symbol = symbol;
        this.totalAmount = totalAmount;
    }
}
