package crypto.order;

import crypto.coin.Coin;
import crypto.coin.CoinService;
import crypto.event.payload.UnifiedEventPayload;
import crypto.fee.FeePolicy;
import crypto.order.exception.NotEnoughBalanceException;
import crypto.order.exception.NotEnoughQuantityException;
import crypto.order.request.LimitOrderServiceRequest;
import crypto.order.request.MarketBuyOrderServiceRequest;
import crypto.order.request.MarketSellOrderServiceRequest;
import crypto.order.response.*;
import crypto.outboxmessagerelay.OutboxEventPublisher;
import crypto.time.TimeProvider;
import crypto.user.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static crypto.event.EventType.*;
import static crypto.order.OrderSide.*;
import static crypto.order.OrderStatus.*;


@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderQueryService orderQueryService;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CoinService coinService;
    private final UserCoinService userCoinService;
    private final TimeProvider timeProvider;
    private final FeePolicy feePolicy;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public OrderCreateResponse createLimitBuyOrder(LimitOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        UserBalance userBalance = user.getUserBalance();
        LocalDateTime registeredDateTime = timeProvider.now();
        BigDecimal totalOrderPrice = request.getPrice().multiply(request.getQuantity());
        BigDecimal orderFee = calculateOrderFee(totalOrderPrice);

        if (userBalance.getAvailableBalance().compareTo(totalOrderPrice.add(orderFee)) < 0) {
            throw new NotEnoughBalanceException();
        }

        userBalance.increaseLockedBalance(totalOrderPrice.add(orderFee));
        Order order = orderRepository.save(buildLimitOrder(request, BUY, user, registeredDateTime));

        outboxEventPublisher.publish(
                LIMIT_BUY_ORDER_CREATE,
                UnifiedEventPayload.builder()
                        .orderId(order.getId())
                        .coinId(order.getCoin().getId())
                        .price(order.getPrice())
                        .build(),
                order.getId()
        );

        return OrderCreateResponse.of(order);
    }

    @Transactional
    public OrderCreateResponse createLimitSellOrder(LimitOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime registeredDateTime = timeProvider.now();
        BigDecimal sellQuantity = request.getQuantity();
        UserCoin userCoin = userCoinService.getUserCoinOrThrow(user, request.getSymbol());

        if (userCoin.getAvailableQuantity().compareTo(sellQuantity) < 0) {
            throw new NotEnoughQuantityException();
        }

        userCoin.increaseLockedQuantity(sellQuantity);
        Order order = orderRepository.save(buildLimitOrder(request, SELL, user, registeredDateTime));

        outboxEventPublisher.publish(
                LIMIT_SELL_ORDER_CREATE,
                UnifiedEventPayload.builder()
                        .orderId(order.getId())
                        .coinId(order.getCoin().getId())
                        .price(order.getPrice())
                        .build(),
                order.getId()
        );

        return OrderCreateResponse.of(order);
    }

    @Transactional
    public OrderCreateResponse createMarketBuyOrder(MarketBuyOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        UserBalance userBalance = user.getUserBalance();
        LocalDateTime registeredDateTime = timeProvider.now();
        BigDecimal totalPrice = request.getTotalPrice();
        Coin coin = coinService.getCoinOrThrow(request.getSymbol());
        BigDecimal orderFee = calculateOrderFee(totalPrice);

        if (userBalance.getAvailableBalance().compareTo(totalPrice.add(orderFee)) < 0) {
            throw new NotEnoughBalanceException();
        }

        userBalance.increaseLockedBalance(totalPrice.add(orderFee));
        Order order = orderRepository.save(Order.createMarketBuyOrder(totalPrice, coin, user, registeredDateTime));

        outboxEventPublisher.publish(
                MARKET_BUY_ORDER_CREATE,
                UnifiedEventPayload.builder()
                        .orderId(order.getId())
                        .coinId(order.getCoin().getId())
                        .marketTotalPrice(order.getMarKetTotalPrice())
                        .build(),
                order.getId()
        );

        return OrderCreateResponse.of(order);
    }

    @Transactional
    public OrderCreateResponse createMarketSellOrder(MarketSellOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime registeredDateTime = timeProvider.now();
        BigDecimal totalAmount = request.getTotalAmount();
        UserCoin userCoin = userCoinService.getUserCoinOrThrow(user, request.getSymbol());

        if (userCoin.getAvailableQuantity().compareTo(totalAmount) < 0) {
            throw new NotEnoughQuantityException();
        }

        userCoin.increaseLockedQuantity(totalAmount);
        Order order = orderRepository.save(Order.createMarketSellOrder(totalAmount, userCoin.getCoin(), user, registeredDateTime));

        outboxEventPublisher.publish(
                MARKET_SELL_ORDER_CREATE,
                UnifiedEventPayload.builder()
                        .orderId(order.getId())
                        .coinId(order.getCoin().getId())
                        .marketTotalQuantity(order.getMarketTotalQuantity())
                        .build(),
                order.getId()
        );

        return OrderCreateResponse.of(order);
    }

    @Transactional
    public OrderDeleteResponse deleteOrder(Long orderId) {
        LocalDateTime deletedDateTime = timeProvider.now();

        Order order = orderQueryService.findOrder(orderId);
        order.markDeleted(deletedDateTime);

        return OrderDeleteResponse.of(order);
    }

    @Transactional(readOnly = true)
    public OrderAvailableResponse getAvailableAmount() {
        User user = userService.getCurrentUser();

        return OrderAvailableResponse.of(user);
    }

    @Transactional(readOnly = true)
    public Page<CompleteOrderListResponse> getCompleteOrders(Pageable pageable) {

        return orderRepository.findByUserIdAndOrderStatus(userService.getCurrentUser().getId(), FILLED, pageable)
                .map(CompleteOrderListResponse::of);
    }

    @Transactional(readOnly = true)
    public Page<OpenOrderListResponse> getOpenOrders(Pageable pageable) {

        return orderRepository.findByUserIdAndOrderStatus(userService.getCurrentUser().getId(), OPEN, pageable)
                .map(OpenOrderListResponse::of);
    }

    private Order buildLimitOrder(LimitOrderServiceRequest request, OrderSide orderSide, User user, LocalDateTime registeredDateTime) {
        String symbol = request.getSymbol();
        BigDecimal price = request.getPrice();
        BigDecimal quantity = request.getQuantity();

        Coin coin = coinService.getCoinOrThrow(symbol);

        return Order.createLimitOrder(price, quantity, orderSide, coin, user, registeredDateTime);
    }

    private BigDecimal calculateOrderFee(BigDecimal totalPrice) {
        BigDecimal feeRate = feePolicy.getTakerFeeRate();

        return totalPrice.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }
}
