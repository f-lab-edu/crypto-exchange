package crypto.wallet.controller.request;

import crypto.wallet.service.request.CheckQuantityServiceRequest;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class CheckQuantityRequest {

    private String symbol;
    private BigDecimal quantity;

    @Builder
    public CheckQuantityRequest(String symbol, BigDecimal quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public CheckQuantityServiceRequest toServiceRequest() {
        return CheckQuantityServiceRequest.builder()
                .symbol(symbol)
                .quantity(quantity)
                .build();
    }
}
