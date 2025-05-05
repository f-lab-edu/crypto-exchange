package crypto.order.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Getter
@SuperBuilder
@NoArgsConstructor
public class LimitOrderRequest extends OrderRequest {

    private Integer price;
    private BigDecimal quantity;
}
