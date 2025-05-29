package crypto.trade;

import crypto.coin.Coin;
import crypto.coin.CoinRepository;
import crypto.order.Order;
import crypto.time.TimeProvider;
import crypto.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.order.OrderSide.*;
import static java.math.BigDecimal.valueOf;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = crypto.AppApiApplication.class)
class TradeSettlementServiceTest {

    @Autowired
    private TradeSettlementService tradeSettlementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private CoinRepository coinRepository;

    @MockitoBean
    private TimeProvider timeProvider;


    @DisplayName("매수 주문에 대한 정산을 실행한다.")
    @Test
    void checkBuyOrderSettle() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);

        User buyer = userRepository.save(User.createUser("buyer@email.com", valueOf(1000)));
        User seller = userRepository.save(User.createUser("seller@email.com", valueOf(1000)));
        BigDecimal takerTotalUsed = valueOf(500);
        BigDecimal makerTotalUsed = valueOf(450);
        BigDecimal orderQuantity = valueOf(2);
        buyer.increaseLockedBalance(takerTotalUsed);

        Coin coin = coinRepository.save(Coin.create("BTC", "비트코인"));
        UserCoin buyerCoin = userCoinRepository.save(UserCoin.create(buyer, coin, valueOf(10)));
        UserCoin sellerCoin = userCoinRepository.save(UserCoin.create(seller, coin, valueOf(5)));
        sellerCoin.increaseLockedQuantity(orderQuantity);

        Order buyOrder = Order.createLimitOrder(valueOf(100), orderQuantity, BUY, coin, buyer, registeredDateTime);
        Order sellOrder = Order.createLimitOrder(valueOf(100), orderQuantity, SELL, coin, seller, registeredDateTime);

        Trade trade = Trade.create(coin.getSymbol(), takerTotalUsed, buyOrder.getQuantity(), BUY,
                buyOrder.getId(), sellOrder.getId(), buyer.getId(), seller.getId(), valueOf(10),
                valueOf(10), registeredDateTime);

        // when
        tradeSettlementService.buyOrderSettle(takerTotalUsed, makerTotalUsed, trade, buyOrder, sellOrder);

        // then
        assertThat(buyer.getLockedBalance()).isEqualByComparingTo(valueOf(0));
        assertThat(buyer.getAvailableBalance()).isEqualByComparingTo(valueOf(500));
        assertThat(seller.getAvailableBalance()).isEqualByComparingTo(valueOf(1450));
        assertThat(buyerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(12));
        assertThat(sellerCoin.getLockedQuantity()).isEqualByComparingTo(valueOf(0));
        assertThat(sellerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(3));
    }

    @DisplayName("매도 주문에 대한 정산을 실행한다.")
    @Test
    void checkSellOrderSettle() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);

        User seller = userRepository.save(User.createUser("seller@email.com", valueOf(1000)));
        User buyer = userRepository.save(User.createUser("buyer@email.com", valueOf(1000)));
        BigDecimal takerTotalUsed = valueOf(450);
        BigDecimal makerTotalUsed = valueOf(500);
        BigDecimal orderQuantity = valueOf(2);
        buyer.increaseLockedBalance(makerTotalUsed);

        Coin coin = coinRepository.save(Coin.create("BTC", "비트코인"));
        UserCoin sellerCoin = userCoinRepository.save(UserCoin.create(seller, coin, valueOf(10)));
        UserCoin buyerCoin = userCoinRepository.save(UserCoin.create(buyer, coin, valueOf(5)));
        sellerCoin.increaseLockedQuantity(orderQuantity);

        Order sellOrder = Order.createLimitOrder(valueOf(100), orderQuantity, SELL, coin, seller, registeredDateTime);
        Order buyOrder = Order.createLimitOrder(valueOf(100), orderQuantity, BUY, coin, buyer, registeredDateTime);

        Trade trade = Trade.create(coin.getSymbol(), takerTotalUsed, buyOrder.getQuantity(), SELL,
                buyOrder.getId(), sellOrder.getId(), buyer.getId(), seller.getId(), valueOf(10),
                valueOf(10), registeredDateTime);

        // when
        tradeSettlementService.sellOrderSettle(takerTotalUsed, makerTotalUsed, trade, sellOrder, buyOrder);

        // then
        assertThat(buyer.getLockedBalance()).isEqualByComparingTo(valueOf(0));
        assertThat(buyer.getAvailableBalance()).isEqualByComparingTo(valueOf(500));
        assertThat(seller.getAvailableBalance()).isEqualByComparingTo(valueOf(1450));
        assertThat(buyerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(7));
        assertThat(sellerCoin.getLockedQuantity()).isEqualByComparingTo(valueOf(0));
        assertThat(sellerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(8));
    }
}