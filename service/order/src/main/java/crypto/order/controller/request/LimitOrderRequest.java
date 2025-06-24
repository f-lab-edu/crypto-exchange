package crypto.order.controller.request;

import crypto.order.service.order.request.LimitOrderServiceRequest;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class LimitOrderRequest {

    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;

    @Builder
    public LimitOrderRequest(String symbol, BigDecimal price, BigDecimal quantity) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
    }

    public LimitOrderServiceRequest toServiceRequest() {
        return LimitOrderServiceRequest.builder()
                .symbol(symbol)
                .price(price)
                .quantity(quantity)
                .build();
    }
}
