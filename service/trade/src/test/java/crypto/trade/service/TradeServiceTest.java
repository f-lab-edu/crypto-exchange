package crypto.trade.service;

import crypto.coin.Coin;
import crypto.coin.CoinRepository;
import crypto.order.Order;
import crypto.order.OrderRepository;
import crypto.order.OrderSide;
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
import java.util.List;

import static crypto.order.OrderSide.*;
import static crypto.order.OrderStatus.*;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = crypto.AppApiApplication.class)
class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @MockitoBean
    private TimeProvider timeProvider;

    @DisplayName("지정가 매수 주문에 대한 체결 처리를 진행한다.")
    @Test
    void checkLimitBuyOrderTrade() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);
        Coin coin = createCoin("BTC", "비트코인");

        User buyer = createUser("buyer@email.com");
        User seller1 = createUser("seller1@email.com");
        User seller2 = createUser("seller2@email.com");
        User seller3 = createUser("seller3@email.com");

        UserBalance buyerBalance = buyer.getUserBalance();
        buyerBalance.increaseAvailableBalance(valueOf(10000));
        buyerBalance.increaseLockedBalance(valueOf(5200));

        UserCoin buyerCoin = createUserCoin(buyer, coin);
        UserCoin sellerCoin1 = createUserCoin(seller1, coin);
        UserCoin sellerCoin2 = createUserCoin(seller2, coin);
        UserCoin sellerCoin3 = createUserCoin(seller3, coin);

        Order buyOrder = createLmitOrder(valueOf(1000), valueOf(5), BUY, coin, buyer, registeredDateTime);
        Order sellOrder1 = createLmitOrder(valueOf(900), valueOf(2), SELL, coin, seller1, registeredDateTime);
        Order sellOrder2 = createLmitOrder(valueOf(1000), valueOf(2), SELL, coin, seller2, registeredDateTime);
        Order sellOrder3 = createLmitOrder(valueOf(1000), valueOf(5), SELL, coin, seller3, registeredDateTime);

        // when
        tradeService.limitBuyOrderMatch(buyOrder);

        // then
        assertThat(sellOrder1.getFilledQuantity()).isEqualByComparingTo(valueOf(2));
        assertThat(sellOrder2.getFilledQuantity()).isEqualByComparingTo(valueOf(2));
        assertThat(sellOrder3.getFilledQuantity()).isEqualByComparingTo(valueOf(1));
        assertThat(buyOrder.getFilledQuantity()).isEqualByComparingTo(valueOf(5));

        assertThat(sellOrder1.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(sellOrder2.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(sellOrder3.getOrderStatus()).isEqualByComparingTo(OPEN);
        assertThat(buyOrder.getOrderStatus()).isEqualByComparingTo(FILLED);

        List<Trade> trades = tradeRepository.findAll();
        assertThat(trades).extracting("symbol")
                .containsExactlyInAnyOrder("BTC", "BTC", "BTC");
        assertThat(trades).extracting("price")
                .containsExactlyInAnyOrder(valueOf(900), valueOf(1000), valueOf(1000));
        assertThat(trades).extracting("quantity")
                .containsExactlyInAnyOrder(valueOf(2), valueOf(2), valueOf(1));
        assertThat(trades).extracting("orderSide")
                .containsExactlyInAnyOrder(BUY, BUY, BUY);
        assertThat(trades).extracting("takerOrderId")
                .containsExactlyInAnyOrder(buyOrder.getId(), buyOrder.getId(), buyOrder.getId());
        assertThat(trades).extracting("makerOrderId")
                .containsExactlyInAnyOrder(sellOrder1.getId(), sellOrder2.getId(), sellOrder3.getId());
        assertThat(trades).extracting("takerUserId")
                .containsExactlyInAnyOrder(buyer.getId(), buyer.getId(), buyer.getId());
        assertThat(trades).extracting("makerUserId")
                .containsExactlyInAnyOrder(seller1.getId(), seller2.getId(), seller3.getId());
        assertThat(trades).extracting("takerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(72), valueOf(80), valueOf(40));
        assertThat(trades).extracting("makerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(72), valueOf(80), valueOf(40));
    }

    @DisplayName("지정가 매도 주문에 대한 체결 처리를 진행한다.")
    @Test
    void checkLimitSellOrderTrade() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);
        Coin coin = createCoin("BTC", "비트코인");

        User seller = createUser("seller@email.com");
        User buyer1 = createUser("buyer1@email.com");
        User buyer2 = createUser("buyer2@email.com");
        User buyer3 = createUser("buyer3@email.com");

        UserBalance buyerBalance1 = buyer1.getUserBalance();
        UserBalance buyerBalance2 = buyer2.getUserBalance();
        UserBalance buyerBalance3 = buyer3.getUserBalance();

        buyerBalance1.increaseAvailableBalance(valueOf(10000));
        buyerBalance1.increaseLockedBalance(valueOf(4992));
        buyerBalance2.increaseAvailableBalance(valueOf(10000));
        buyerBalance2.increaseLockedBalance(valueOf(4160));
        buyerBalance3.increaseAvailableBalance(valueOf(10000));
        buyerBalance3.increaseLockedBalance(valueOf(1040));

        UserCoin sellerCoin = createUserCoin(seller , coin);
        UserCoin buyerCoin1 = createUserCoin(buyer1, coin);
        UserCoin buyerCoin2 = createUserCoin(buyer2, coin);
        UserCoin buyerCoin3 = createUserCoin(buyer3, coin);

        sellerCoin.increaseQuantity(valueOf(15));
        sellerCoin.increaseLockedQuantity(valueOf(10));

        Order sellOrder = createLmitOrder(valueOf(1000), valueOf(10), SELL, coin, seller, registeredDateTime);
        Order buyOrder1 = createLmitOrder(valueOf(1200), valueOf(4), BUY, coin, buyer1, registeredDateTime);
        Order buyOrder2 = createLmitOrder(valueOf(1000), valueOf(4), BUY, coin, buyer2, registeredDateTime);
        Order buyOrder3 = createLmitOrder(valueOf(1000), valueOf(1), BUY, coin, buyer3, registeredDateTime);

        // when
        tradeService.limitSellOrderMatch(sellOrder);

        // then
        assertThat(buyOrder1.getFilledQuantity()).isEqualByComparingTo(valueOf(4));
        assertThat(buyOrder2.getFilledQuantity()).isEqualByComparingTo(valueOf(4));
        assertThat(buyOrder3.getFilledQuantity()).isEqualByComparingTo(valueOf(1));
        assertThat(sellOrder.getFilledQuantity()).isEqualByComparingTo(valueOf(9));

        assertThat(buyOrder1.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(buyOrder2.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(buyOrder3.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(sellOrder.getOrderStatus()).isEqualByComparingTo(OPEN);

        List<Trade> trades = tradeRepository.findAll();
        assertThat(trades).extracting("symbol")
                .containsExactlyInAnyOrder("BTC", "BTC", "BTC");
        assertThat(trades).extracting("price")
                .containsExactlyInAnyOrder(valueOf(1200), valueOf(1000), valueOf(1000));
        assertThat(trades).extracting("quantity")
                .containsExactlyInAnyOrder(valueOf(4), valueOf(4), valueOf(1));
        assertThat(trades).extracting("orderSide")
                .containsExactlyInAnyOrder(SELL, SELL, SELL);
        assertThat(trades).extracting("takerOrderId")
                .containsExactlyInAnyOrder(sellOrder.getId(), sellOrder.getId(), sellOrder.getId());
        assertThat(trades).extracting("makerOrderId")
                .containsExactlyInAnyOrder(buyOrder1.getId(), buyOrder2.getId(), buyOrder3.getId());
        assertThat(trades).extracting("takerUserId")
                .containsExactlyInAnyOrder(seller.getId(), seller.getId(), seller.getId());
        assertThat(trades).extracting("makerUserId")
                .containsExactlyInAnyOrder(buyer1.getId(), buyer2.getId(), buyer3.getId());
        assertThat(trades).extracting("takerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(192), valueOf(160), valueOf(40));
        assertThat(trades).extracting("makerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(192), valueOf(160), valueOf(40));
    }

    @DisplayName("시장가 매수 주문에 대한 체결 처리를 진행한다.")
    @Test
    void checkMarketBuyOrderTrade() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);
        Coin coin = createCoin("BTC", "비트코인");

        User buyer = createUser("buyer@email.com");
        User seller1 = createUser("seller1@email.com");
        User seller2 = createUser("seller2@email.com");
        User seller3 = createUser("seller3@email.com");

        UserBalance buyerBalance = buyer.getUserBalance();
        buyerBalance.increaseAvailableBalance(valueOf(15000));
        buyerBalance.increaseLockedBalance(valueOf(10400));

        UserCoin sellerCoin1 = createUserCoin(seller1, coin);
        UserCoin sellerCoin2 = createUserCoin(seller2, coin);
        UserCoin sellerCoin3 = createUserCoin(seller3, coin);

        Order buyOrder = createMarketOrder(valueOf(10000), coin, buyer, BUY, registeredDateTime);
        Order sellOrder1 = createLmitOrder(valueOf(1000), valueOf(4), SELL, coin, seller1, registeredDateTime);
        Order sellOrder2 = createLmitOrder(valueOf(1200), valueOf(4), SELL, coin, seller2, registeredDateTime);
        Order sellOrder3 = createLmitOrder(valueOf(1200), valueOf(3), SELL, coin, seller3, registeredDateTime);

        // when
        tradeService.marketBuyOrderMatch(buyOrder);

        // then
        assertThat(sellOrder1.getFilledQuantity()).isEqualByComparingTo(valueOf(4));
        assertThat(sellOrder2.getFilledQuantity()).isEqualByComparingTo(valueOf(4));
        assertThat(sellOrder3.getFilledQuantity()).isEqualByComparingTo(valueOf(0));
        assertThat(buyOrder.getFilledQuantity()).isEqualByComparingTo(valueOf(8));

        assertThat(sellOrder1.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(sellOrder2.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(sellOrder3.getOrderStatus()).isEqualByComparingTo(OPEN);
        assertThat(buyOrder.getOrderStatus()).isEqualByComparingTo(OPEN);

        List<Trade> trades = tradeRepository.findAll();
        assertThat(trades).extracting("symbol")
                .containsExactlyInAnyOrder("BTC", "BTC");
        assertThat(trades).extracting("price")
                .containsExactlyInAnyOrder(valueOf(1000), valueOf(1200));
        assertThat(trades).extracting("quantity")
                .containsExactlyInAnyOrder(valueOf(4), valueOf(4));
        assertThat(trades).extracting("orderSide")
                .containsExactlyInAnyOrder(BUY, BUY);
        assertThat(trades).extracting("takerOrderId")
                .containsExactlyInAnyOrder(buyOrder.getId(), buyOrder.getId());
        assertThat(trades).extracting("makerOrderId")
                .containsExactlyInAnyOrder(sellOrder1.getId(), sellOrder2.getId());
        assertThat(trades).extracting("takerUserId")
                .containsExactlyInAnyOrder(buyer.getId(), buyer.getId());
        assertThat(trades).extracting("makerUserId")
                .containsExactlyInAnyOrder(seller1.getId(), seller2.getId());
        assertThat(trades).extracting("takerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(160), valueOf(192));
        assertThat(trades).extracting("makerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(160), valueOf(192));

        assertThat(buyer.getUserBalance().getLockedBalance()).isEqualByComparingTo(valueOf(0));
    }

    @DisplayName("시장가 매도 주문에 대한 체결 처리를 진행한다.")
    @Test
    void checkMarketSellOrderTrade() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.of(2025, 5, 10, 15, 0);
        when(timeProvider.now()).thenReturn(registeredDateTime);
        Coin coin = createCoin("BTC", "비트코인");

        User seller = createUser("buyer@email.com");
        User buyer1 = createUser("buyer1@email.com");
        User buyer2 = createUser("buyer2@email.com");
        User buyer3 = createUser("buyer3@email.com");

        UserBalance buyerBalance1 = buyer1.getUserBalance();
        UserBalance buyerBalance2 = buyer2.getUserBalance();
        UserBalance buyerBalance3 = buyer3.getUserBalance();

        buyerBalance1.increaseAvailableBalance(valueOf(10000));
        buyerBalance1.increaseLockedBalance(valueOf(3744));
        buyerBalance2.increaseAvailableBalance(valueOf(10000));
        buyerBalance2.increaseLockedBalance(valueOf(5200));
        buyerBalance3.increaseAvailableBalance(valueOf(10000));
        buyerBalance3.increaseLockedBalance(valueOf(3120));

        UserCoin sellerCoin = createUserCoin(seller, coin);
        UserCoin buyerCoin1 = createUserCoin(buyer1, coin);
        UserCoin buyerCoin2 = createUserCoin(buyer2, coin);
        UserCoin buyerCoin3 = createUserCoin(buyer3, coin);

        Order sellOrder = createMarketOrder(valueOf(10), coin, seller, SELL, registeredDateTime);
        Order buyOrder1 = createLmitOrder(valueOf(1200), valueOf(3), BUY, coin, buyer1, registeredDateTime);
        Order buyOrder2 = createLmitOrder(valueOf(1000), valueOf(5), BUY, coin, buyer2, registeredDateTime);
        Order buyOrder3 = createLmitOrder(valueOf(1000), valueOf(3), BUY, coin, buyer3, registeredDateTime);

        // when
        tradeService.marketSellOrderMatch(sellOrder);

        // then
        assertThat(buyOrder1.getFilledQuantity()).isEqualByComparingTo(valueOf(3));
        assertThat(buyOrder2.getFilledQuantity()).isEqualByComparingTo(valueOf(5));
        assertThat(buyOrder3.getFilledQuantity()).isEqualByComparingTo(valueOf(2));
        assertThat(sellOrder.getFilledQuantity()).isEqualByComparingTo(valueOf(10));

        assertThat(buyOrder1.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(buyOrder2.getOrderStatus()).isEqualByComparingTo(FILLED);
        assertThat(buyOrder3.getOrderStatus()).isEqualByComparingTo(OPEN);
        assertThat(sellOrder.getOrderStatus()).isEqualByComparingTo(OPEN);

        List<Trade> trades = tradeRepository.findAll();
        assertThat(trades).extracting("symbol")
                .containsExactlyInAnyOrder("BTC", "BTC", "BTC");
        assertThat(trades).extracting("price")
                .containsExactlyInAnyOrder(valueOf(1200), valueOf(1000), valueOf(1000));
        assertThat(trades).extracting("quantity")
                .containsExactlyInAnyOrder(valueOf(3), valueOf(5), valueOf(2));
        assertThat(trades).extracting("orderSide")
                .containsExactlyInAnyOrder(SELL, SELL, SELL);
        assertThat(trades).extracting("takerOrderId")
                .containsExactlyInAnyOrder(sellOrder.getId(), sellOrder.getId(), sellOrder.getId());
        assertThat(trades).extracting("makerOrderId")
                .containsExactlyInAnyOrder(buyOrder1.getId(), buyOrder2.getId(), buyOrder3.getId());
        assertThat(trades).extracting("takerUserId")
                .containsExactlyInAnyOrder(seller.getId(), seller.getId(), seller.getId());
        assertThat(trades).extracting("makerUserId")
                .containsExactlyInAnyOrder(buyer1.getId(), buyer2.getId(), buyer3.getId());
        assertThat(trades).extracting("takerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(144), valueOf(200), valueOf(80));
        assertThat(trades).extracting("makerFee")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(valueOf(144), valueOf(200), valueOf(80));
    }

    private User createUser(String email) {
        return userRepository.save(User.createUser(email));
    }

    private Order createLmitOrder(BigDecimal price, BigDecimal quantity, OrderSide orderSide, Coin coin, User user, LocalDateTime dateTime) {
        return orderRepository.save(Order.createLimitOrder(price, quantity, orderSide, coin, user, dateTime));
    }

    private Order createMarketOrder(BigDecimal totalAmount, Coin coin, User user, OrderSide orderSide, LocalDateTime dateTime) {
        if (orderSide.equals(BUY)) {
            return orderRepository.save(Order.createMarketBuyOrder(totalAmount, coin, user, dateTime));
        } else {
            return orderRepository.save(Order.createMarketSellOrder(totalAmount, coin, user, dateTime));
        }
    }

    private Coin createCoin(String symbol, String name) {
        return coinRepository.save(Coin.create(symbol, name));
    }

    private UserCoin createUserCoin(User user, Coin coin) {
        return userCoinRepository.save(UserCoin.create(user, coin));
    }
}