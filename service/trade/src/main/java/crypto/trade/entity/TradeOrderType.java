package crypto.trade.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum TradeOrderType {

    LIMIT("지정가 주문"),
    MARKET("시장가 주문");

    private final String text;
}
