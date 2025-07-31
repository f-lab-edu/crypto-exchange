package crypto.event.eventsender;

import crypto.dataserializer.DataSerializer;
import crypto.event.payload.EventPayload;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static crypto.event.EventType.LIMIT_BUY_ORDER_CREATE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderEventSenderTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private DataSerializer dataSerializer;

    @Spy
    @InjectMocks
    private OrderEventSender orderEventSender;

    @DisplayName("메시지 전송 성공 시, 성공 로그를 기록하고 DLQ로 보내지 않는다.")
    @Test
    void sendSuccess() {
        // given
        EventPayload payload = EventPayload.builder()
                .userId(42L)
                .symbol("BTC")
                .build();

        String message =
                "{\"eventId\":\"a1b2c3d4-e5f6-7890-1234-567890ab1def\"," +
                        "\"type\":\"LIMIT_BUY_ORDER_CREATE\"," +
                        "\"payload\":{" +
                        "\"userId\":42," +
                        "\"symbol\":\"BTC\"," +
                        "}}";

        when(dataSerializer.serialize(any())).thenReturn(message);

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(
                LIMIT_BUY_ORDER_CREATE.getTopic(), 0), 0, 0, 0, 0, 0
        );
        SendResult<String, String> sendResult = new SendResult<>(new ProducerRecord<>(LIMIT_BUY_ORDER_CREATE.getTopic(), message), recordMetadata);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // when
        orderEventSender.send(LIMIT_BUY_ORDER_CREATE, payload);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, timeout(1000)).send(topicCaptor.capture(), messageCaptor.capture());
        assertThat(LIMIT_BUY_ORDER_CREATE.getTopic()).isEqualTo(topicCaptor.getValue());
        assertThat(message).isEqualTo(messageCaptor.getValue());

        verify(orderEventSender, never()).sendToDeadLetterQueue(anyString(), anyString());
    }

    @DisplayName("메시지 전송 실패 시, 실패 로그를 기록하고 DLQ로 보낸다.")
    @Test
    void sendFailure() {
        // given
        EventPayload payload = EventPayload.builder()
                .userId(42L)
                .symbol("BTC")
                .build();

        String message =
                "{\"eventId\":\"a1b2c3d4-e5f6-7890-1234-567890ab1def\"," +
                        "\"type\":\"LIMIT_BUY_ORDER_CREATE\"," +
                        "\"payload\":{" +
                        "\"userId\":42," +
                        "\"symbol\":\"BTC\"," +
                        "}}";

        when(dataSerializer.serialize(any())).thenReturn(message);

        KafkaException kafkaException = new KafkaException("Failed to connect to Kafka broker");
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(kafkaException);

        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(failedFuture);

        // when
        orderEventSender.send(LIMIT_BUY_ORDER_CREATE, payload);

        // then
        verify(orderEventSender, timeout(1000)).sendToDeadLetterQueue(
                eq(message),
                eq("ERROR_SENDING_ORDER_EVENT")
        );
    }
}