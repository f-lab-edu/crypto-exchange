package crypto.trade.repository;

import crypto.trade.entity.TradeOrder;
import crypto.trade.entity.TradeOrderSide;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static crypto.trade.entity.TradeOrderSide.*;
import static java.math.BigDecimal.valueOf;


@DataJpaTest
@Transactional
class TradeOrderRepositoryTest {

    @Autowired
    private TradeOrderRepository tradeOrderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TradeOrder buyOrder1;
    private TradeOrder buyOrder2;
    private TradeOrder sellOrder1;
    private TradeOrder sellOrder2;
    private TradeOrder filledOrder;

    @BeforeEach
    void setUp() {
        // 매수 주문
        buyOrder1 = TradeOrder.builder()
                .userId(1L).symbol("BTC").orderSide(BUY)
                .price(valueOf(1000)).quantity(valueOf(5)).filledQuantity(BigDecimal.ZERO)
                .orderStatus(TradeOrderStatus.OPEN).registeredDateTime(LocalDateTime.of(2025, 7, 23, 10, 0, 0))
                .build();
        entityManager.persistAndFlush(buyOrder1);

        buyOrder2 = TradeOrder.builder()
                .userId(2L).symbol("BTC").orderSide(BUY)
                .price(valueOf(1010)).quantity(valueOf(3)).filledQuantity(BigDecimal.ZERO)
                .orderStatus(TradeOrderStatus.OPEN).registeredDateTime(LocalDateTime.of(2025, 7, 23, 10, 5, 0))
                .build();
        entityManager.persistAndFlush(buyOrder2);

        // 매도 주문
        sellOrder1 = TradeOrder.builder()
                .userId(3L).symbol("BTC").orderSide(SELL)
                .price(valueOf(1050)).quantity(valueOf(2)).filledQuantity(BigDecimal.ZERO)
                .orderStatus(TradeOrderStatus.OPEN).registeredDateTime(LocalDateTime.of(2025, 7, 23, 10, 10, 0))
                .build();
        entityManager.persistAndFlush(sellOrder1);

        sellOrder2 = TradeOrder.builder()
                .userId(4L).symbol("BTC").orderSide(SELL)
                .price(valueOf(1040)).quantity(valueOf(4)).filledQuantity(BigDecimal.ZERO)
                .orderStatus(TradeOrderStatus.OPEN).registeredDateTime(LocalDateTime.of(2025, 7, 23, 10, 15, 0))
                .build();
        entityManager.persistAndFlush(sellOrder2);

        // 이미 체결된 주문 (OPEN이 아니므로 쿼리 결과에 포함되면 안됨)
        filledOrder = TradeOrder.builder()
                .userId(5L).symbol("BTC").orderSide(BUY)
                .price(valueOf(990)).quantity(valueOf(1)).filledQuantity(valueOf(1))
                .orderStatus(TradeOrderStatus.CLOSED).registeredDateTime(LocalDateTime.of(2025, 7, 23, 9, 0, 0))
                .build();
        entityManager.persistAndFlush(closedOrder);
    }
}