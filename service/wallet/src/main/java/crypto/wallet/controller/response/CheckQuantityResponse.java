package crypto.wallet.controller.response;

import lombok.Getter;


@Getter
public class CheckQuantityResponse {

    private Long userId;
    private boolean checkQuantity = true;

    public CheckQuantityResponse(Long userId) {
        this.userId = userId;
    }

    public static CheckQuantityResponse of(Long userId) {
        return new CheckQuantityResponse(userId);
    }
}
