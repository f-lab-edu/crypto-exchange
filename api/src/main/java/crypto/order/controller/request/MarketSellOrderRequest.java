package crypto.order.controller.request;

import crypto.order.service.order.request.MarketSellOrderServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class MarketSellOrderRequest {

    private String symbol;
    private BigDecimal totalAmount;

    @Builder
    public MarketSellOrderRequest(String symbol, BigDecimal totalAmount) {
        this.symbol = symbol;
        this.totalAmount = totalAmount;
    }

    public MarketSellOrderServiceRequest toServiceRequest() {
        return MarketSellOrderServiceRequest.builder()
                .symbol(symbol)
                .totalAmount(totalAmount)
                .build();
    }
}
