package crypto.trade.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeOrderSide {

    BUY("매수"),
    SELL("매도");

    private final String text;
}
