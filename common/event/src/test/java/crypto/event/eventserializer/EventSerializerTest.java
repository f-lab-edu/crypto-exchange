package crypto.event.eventserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import crypto.dataserializer.DataSerializer;
import crypto.event.Event;
import crypto.event.payload.EventPayload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static crypto.event.EventType.*;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.*;


public class EventSerializerTest {

    DataSerializer dataSerializer;

    @BeforeEach
    void setUp() {
        this.dataSerializer = new DataSerializer((new ObjectMapper()));
    }

    @DisplayName("json 문자열을 Event 객체로 역직렬화 한다.")
    @Test
    void deserializeSuccess() {
        // given
        String message =
                "{\"eventId\":\"a1b2c3d4-e5f6-7890-1234-567890ab1def\"," +
                        "\"type\":\"LIMIT_BUY_ORDER_CREATE\"," +
                        "\"payload\":{" +
                        "\"userId\":42," +
                        "\"symbol\":\"BTC\"," +
                        "\"price\":50000," +
                        "\"quantity\":10" +
                        "}}";

        // when
        Event event = dataSerializer.deserialize(message, Event.class);

        // then
        assertThat(event.getEventId()).isEqualTo("a1b2c3d4-e5f6-7890-1234-567890ab1def");
        assertThat(event.getType()).isEqualTo(LIMIT_BUY_ORDER_CREATE);

        EventPayload payload = event.getPayload();
        assertThat(payload.getUserId()).isEqualTo(42L);
        assertThat(payload.getSymbol()).isEqualTo("BTC");
        assertThat(payload.getPrice()).isEqualTo(valueOf(50000));
        assertThat(payload.getQuantity()).isEqualTo(valueOf(10));
    }

    @DisplayName("Event 객체를 json 문자열로 직렬화 한다.")
    @Test
    void serializeSuccess() {
        // given
        String eventId = "a1b2c3d4-e5f6-7890-1234-567890ab1def";

        EventPayload payload = EventPayload.builder()
                .userId(42L)
                .symbol("BTC")
                .price(valueOf(50000))
                .quantity(valueOf(10))
                .build();

        Event event = Event.of(eventId, LIMIT_BUY_ORDER_CREATE, payload);

        // when
        String message = dataSerializer.serialize(event);

        // then
        assertThat(message).isNotBlank();
        Event deserializedEvent = dataSerializer.deserialize(message, Event.class);

        assertThat(deserializedEvent.getEventId()).isEqualTo(event.getEventId());
        assertThat(deserializedEvent.getType()).isEqualTo(event.getType());
        assertThat(deserializedEvent.getPayload().getUserId()).isEqualTo(event.getPayload().getUserId());
        assertThat(deserializedEvent.getPayload().getSymbol()).isEqualTo(event.getPayload().getSymbol());
        assertThat(deserializedEvent.getPayload().getPrice()).isEqualTo(event.getPayload().getPrice());
        assertThat(deserializedEvent.getPayload().getQuantity()).isEqualTo(event.getPayload().getQuantity());
    }

    @DisplayName("유효하지 않은 json 문자열 역직렬화 시 null을 반환한다.")
    @Test
    void deserializeFailure() {
        // given
        String invalidJson = "{\"eventId\":\"invalid json\"type\":\"LIMIT_BUY_ORDER_CREATE\"}";

        // when
        Event event = dataSerializer.deserialize(invalidJson, Event.class);

        // then
        assertThat(event).isNull(); // null 반환을 검증
    }
}

