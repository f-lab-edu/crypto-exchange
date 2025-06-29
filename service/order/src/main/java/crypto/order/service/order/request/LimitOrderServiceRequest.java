package crypto.order.service.order.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class LimitOrderServiceRequest {

    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;

    @Builder
    public LimitOrderServiceRequest(String symbol, BigDecimal price, BigDecimal quantity) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
    }
}
