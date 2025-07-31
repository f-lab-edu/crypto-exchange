package crypto.order.entity.order.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCycleStatus {

    ORDER_PENDING("주문 진행중"),
    TRADE_PENDING("체결 진행중"),
    COMPLETE("정산 완료"),
    CANCELLED("취소됨");

    private final String text;
}
