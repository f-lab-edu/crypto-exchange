package crypto.settlement.service;

import crypto.coin.Coin;
import crypto.coin.CoinRepository;
import crypto.order.Order;
import crypto.time.TimeProvider;
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


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = crypto.AppApiApplication.class)
class SettlementServiceTest {

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


    @DisplayName("매수 주문에 대한 유저의 잔액 및 코인 정산을 실행한다.")
    @Test
    void checkBuyOrderSettle() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);

        User buyer = createUser("buyer@email.com");
        User seller = createUser("seller@email.com");
        UserBalance buyerBalance = buyer.getUserBalance();
        UserBalance sellerBalance = seller.getUserBalance();
        buyerBalance.increaseAvailableBalance(valueOf(1000));
        sellerBalance.increaseAvailableBalance(valueOf(1000));

        BigDecimal takerTotalUsed = valueOf(500);
        BigDecimal makerTotalUsed = valueOf(450);
        BigDecimal orderQuantity = valueOf(2);
        buyerBalance.increaseLockedBalance(takerTotalUsed);

        Coin coin = createCoin("BTC", "비트코인");
        UserCoin buyerCoin = createUserCoin(buyer, coin);
        UserCoin sellerCoin = createUserCoin(seller, coin);
        sellerCoin.increaseQuantity(valueOf(5));
        sellerCoin.increaseLockedQuantity(orderQuantity);

        Order buyOrder = Order.createLimitOrder(valueOf(100), orderQuantity, BUY, coin, buyer, registeredDateTime);
        Order sellOrder = Order.createLimitOrder(valueOf(100), orderQuantity, SELL, coin, seller, registeredDateTime);

        Trade trade = Trade.create(coin.getSymbol(), takerTotalUsed, buyOrder.getQuantity(), BUY,
                buyOrder.getId(), sellOrder.getId(), buyer.getId(), seller.getId(), valueOf(10),
                valueOf(10), registeredDateTime);

        // when
        tradeSettlementService.buyOrderSettle(takerTotalUsed, makerTotalUsed, trade, buyOrder, sellOrder);

        // then
        assertThat(buyerBalance.getLockedBalance()).isEqualByComparingTo(valueOf(0));
        assertThat(buyerBalance.getAvailableBalance()).isEqualByComparingTo(valueOf(500));
        assertThat(sellerBalance.getAvailableBalance()).isEqualByComparingTo(valueOf(1450));
        assertThat(buyerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(2));
        assertThat(sellerCoin.getLockedQuantity()).isEqualByComparingTo(valueOf(0));
        assertThat(sellerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(3));
    }

    @DisplayName("매도 주문에 대한 유저의 잔액 및 코인 정산을 실행한다.")
    @Test
    void checkSellOrderSettle() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);

        User seller = createUser("seller@email.com");
        User buyer = createUser("buyer@email.com");
        UserBalance buyerBalance = buyer.getUserBalance();
        UserBalance sellerBalance = seller.getUserBalance();
        buyerBalance.increaseAvailableBalance(valueOf(1000));
        sellerBalance.increaseAvailableBalance(valueOf(1000));

        BigDecimal takerTotalUsed = valueOf(450);
        BigDecimal makerTotalUsed = valueOf(500);
        BigDecimal orderQuantity = valueOf(2);
        buyerBalance.increaseLockedBalance(makerTotalUsed);

        Coin coin = createCoin("BTC", "비트코인");
        UserCoin sellerCoin = createUserCoin(seller, coin);
        UserCoin buyerCoin = createUserCoin(buyer, coin);
        sellerCoin.increaseQuantity(valueOf(10));
        sellerCoin.increaseLockedQuantity(orderQuantity);

        Order sellOrder = Order.createLimitOrder(valueOf(100), orderQuantity, SELL, coin, seller, registeredDateTime);
        Order buyOrder = Order.createLimitOrder(valueOf(100), orderQuantity, BUY, coin, buyer, registeredDateTime);

        Trade trade = Trade.create(coin.getSymbol(), takerTotalUsed, buyOrder.getQuantity(), SELL,
                buyOrder.getId(), sellOrder.getId(), buyer.getId(), seller.getId(), valueOf(10),
                valueOf(10), registeredDateTime);

        // when
        tradeSettlementService.sellOrderSettle(takerTotalUsed, makerTotalUsed, trade, sellOrder, buyOrder);

        // then
        assertThat(buyerBalance.getLockedBalance()).isEqualByComparingTo(valueOf(0));
        assertThat(buyerBalance.getAvailableBalance()).isEqualByComparingTo(valueOf(500));
        assertThat(sellerBalance.getAvailableBalance()).isEqualByComparingTo(valueOf(1450));
        assertThat(buyerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(2));
        assertThat(sellerCoin.getLockedQuantity()).isEqualByComparingTo(valueOf(0));
        assertThat(sellerCoin.getAvailableQuantity()).isEqualByComparingTo(valueOf(8));
    }

    private User createUser(String email) {
        return userRepository.save(User.createUser(email));
    }

    private Coin createCoin(String symbol, String name) {
        return coinRepository.save(Coin.create(symbol, name));
    }

    private UserCoin createUserCoin(User user, Coin coin) {
        return userCoinRepository.save(UserCoin.create(user, coin));
    }
}