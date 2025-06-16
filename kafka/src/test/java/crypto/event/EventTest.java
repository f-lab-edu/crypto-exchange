package crypto.event;

import crypto.event.payload.LimitOrderCreateEventPayload;
import org.junit.jupiter.api.Test;

import static crypto.event.EventType.*;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;


class EventTest {

    @Test
    void checkSerializeAndDeserializeEvent() {
        // given
//        LimitOrderCreateEventPayload payload = LimitOrderCreateEventPayload.builder()
//                .orderId(1L)
//                .coinId(1L)
//                .price(valueOf(1000))
//                .build();

//        Event<EventPayload> event = Event.of(
//                "1234",
//                LIMIT_BUY_ORDER_CREATE,
//                payload
//        );
//
//        String json = event.toJson();
//
//        // when
//        Event<EventPayload> result = Event.fromJson(json);
//
//        // then
//        assertThat(result.getEventId()).isEqualTo(event.getEventId());
//        assertThat(result.getType()).isEqualTo(event.getType());
//        assertThat(result.getPayload()).isInstanceOf(payload.getClass());
//
//        LimitOrderCreateEventPayload resultPayload = (LimitOrderCreateEventPayload) result.getPayload();
//        assertThat(resultPayload.getOrderId()).isEqualTo(payload.getOrderId());
//        assertThat(resultPayload.getCoinId()).isEqualTo(payload.getCoinId());
//        assertThat(resultPayload.getPrice()).isEqualTo(payload.getPrice());
    }
}