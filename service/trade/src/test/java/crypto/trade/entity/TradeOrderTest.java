package crypto.trade.entity;

import crypto.trade.entity.exception.FilledQuantityExceedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.trade.entity.TradeOrderSide.*;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.*;

class TradeOrderTest {

    private TradeOrder order;

    @BeforeEach
    void setUp() {
        order = TradeOrder.builder()
            .orderId(1L)
            .userId(1L)
            .symbol("BTC")
            .price(valueOf(1000))
            .quantity(valueOf(10))
            .orderSide(BUY)
            .registeredDateTime(LocalDateTime.now())
            .build();
    }

    @DisplayName("주문 체결 시 해당 주문의 체결 된 수량이 증가한다.")
    @Test
    void checkFilledQuantity() {

        // when
        order.fill(valueOf(5));

        // then
        assertThat(order.getFilledQuantity()).isEqualByComparingTo(valueOf(5));
    }

    @DisplayName("해당 주문의 요청 수량과 체결된 수량을 비교하여 체결 완료된 주문을 확인한다.")
    @Test
    void checkFullyFilledTrue() {
        // given

        // when
        order.fill(valueOf(10));

        // then
        assertThat(order.isFullyFilled()).isTrue();
    }

    @DisplayName("해당 주문의 요청 수량과 체결된 수량을 비교하여 체결 완료 되지 않은 주문을 확인한다.")
    @Test
    void checkFullyFilledFalse() {

        // when
        order.fill(valueOf(9));

        // then
        assertThat(order.isFullyFilled()).isFalse();
    }

    @DisplayName("미체결 주문 조회시 남은 주문 수량을 계산한다.")
    @Test
    void checkRemainQuantity() {

        // when
        order.fill(valueOf(9));
        BigDecimal remainQuantity = order.calculateRemainQuantity();

        // then
        assertThat(remainQuantity).isEqualByComparingTo(valueOf(1));
    }

    @DisplayName("미체결 주문 조회시 남은 주문 수량이 음수가 되는 경우 예외가 발생한다.")
    @Test
    void checkRemainQuantityNegative() {

        // when
        order.fill(valueOf(11));

        // then
        assertThatThrownBy(order::calculateRemainQuantity)
                .isInstanceOf(FilledQuantityExceedException.class)
                .hasMessage("체결 수량이 전체 주문 수량을 초과했습니다.");
    }
}