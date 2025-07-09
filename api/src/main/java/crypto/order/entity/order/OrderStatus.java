package crypto.order.entity.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    FILLED("체결"),
    OPEN("미체결");

    private final String text;
}
