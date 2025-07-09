package crypto.settlement.controller.request;

import crypto.settlement.service.request.CheckBalanceServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class CheckBalanceRequest {

    private BigDecimal price;
    private BigDecimal quantity;

    @Builder
    public CheckBalanceRequest(BigDecimal price, BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public CheckBalanceServiceRequest toServiceRequest() {
        return CheckBalanceServiceRequest.builder()
                .price(price)
                .quantity(quantity)
                .build();
    }
}
