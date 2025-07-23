package crypto.order.entity.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    OPEN("미체결"),
    FILLED("체결");

    private final String text;
}
