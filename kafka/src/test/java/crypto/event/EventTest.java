package crypto.event;

import crypto.event.payload.UnifiedEventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static crypto.event.EventType.*;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;


class EventTest {

    @DisplayName("이벤트를 시리얼라이즈, 디시지얼라이즈하는 과정이 정상적으로 작동해야한다.")
    @Test
    void checkSerializeAndDeserializeEvent() {
        // given
        UnifiedEventPayload payload = UnifiedEventPayload.builder()
                .orderId(1L)
                .coinId(1L)
                .price(valueOf(1000))
                .build();

        Event event = Event.of(
                "1234",
                LIMIT_BUY_ORDER_CREATE,
                payload
        );

        String json = event.toJson();

        // when
        Event result = Event.fromJson(json);

        // then
        assertThat(result.getEventId()).isEqualTo(event.getEventId());
        assertThat(result.getType()).isEqualTo(event.getType());
        assertThat(result.getPayload()).isInstanceOf(payload.getClass());

        UnifiedEventPayload resultPayload = result.getPayload();
        assertThat(resultPayload.getOrderId()).isEqualTo(payload.getOrderId());
        assertThat(resultPayload.getCoinId()).isEqualTo(payload.getCoinId());
        assertThat(resultPayload.getPrice()).isEqualTo(payload.getPrice());
    }
}