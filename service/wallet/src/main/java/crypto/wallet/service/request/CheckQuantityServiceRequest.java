package crypto.wallet.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class CheckQuantityServiceRequest {

    private String symbol;
    private BigDecimal quantity;

    @Builder
    public CheckQuantityServiceRequest(String symbol, BigDecimal quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
    }
}
