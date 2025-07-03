package crypto.wallet.controller.response;

import lombok.Getter;


@Getter
public class CheckBalanceResponse {

    private Long userId;
    private boolean checkBalance = true;

    public CheckBalanceResponse(Long userId) {
        this.userId = userId;
    }

    public static CheckBalanceResponse of(Long userId) {
        return new CheckBalanceResponse(userId);
    }
}
