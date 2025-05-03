package crypto.order.response;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class OrderAvailableResponse {

    private String currency;
    private long amount;
}
