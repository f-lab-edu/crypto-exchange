package crypto.trade.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeOrderStatus {

    FILLED("체결"),
    OPEN("미체결"),
    CANCELLED("취소됨");

    private final String text;
}
