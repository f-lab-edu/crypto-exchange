package crypto.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSide {

    BUY("매수"),
    SELL("매도");

    private final String text;
}
