package crypto.wallet.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@NoArgsConstructor
public class CheckBalanceServiceRequest {

    private BigDecimal price;
    private BigDecimal quantity;

    @Builder
    public CheckBalanceServiceRequest(BigDecimal price, BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }
}
