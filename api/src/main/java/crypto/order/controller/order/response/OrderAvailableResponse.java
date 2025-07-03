package crypto.order.controller.order.response;

import crypto.order.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Builder
public class OrderAvailableResponse {

    private String currency;
    private BigDecimal amount;

    @Builder
    public OrderAvailableResponse(String currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static OrderAvailableResponse of(User user) {
        return OrderAvailableResponse.builder()
                .currency("KRW")
                .amount(user.getUserBalance().getAvailableBalance())
                .build();
    }
}
