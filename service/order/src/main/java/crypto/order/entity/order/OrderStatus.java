package crypto.order.entity.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    OPEN("미체결"),
    ORDER_PENDING("주문 진행중"),
    TRADE_PENDING("체결 진행중"),
    FILLED("체결"),
    CANCELLED("취소됨");


    private final String text;
}
