package crypto.order.service.order;

import crypto.common.time.TimeProvider;
import crypto.event.payload.EventPayload;
import crypto.order.controller.response.*;
import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import crypto.order.entity.user.User;
import crypto.order.entity.user.UserCoin;
import crypto.order.repository.order.OrderRepository;
import crypto.order.service.coin.CoinService;
import crypto.order.service.order.exception.NotEnoughQuantityException;
import crypto.order.service.order.request.LimitOrderServiceRequest;
import crypto.order.service.order.request.MarketBuyOrderServiceRequest;
import crypto.order.service.order.request.MarketSellOrderServiceRequest;
import crypto.order.service.user.UserCoinService;
import crypto.order.service.user.UserService;
import crypto.outboxmessagerelay.OutboxEventPublisher;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.event.EventType.*;
import static crypto.order.entity.order.OrderSide.*;
import static crypto.order.entity.order.OrderStatus.*;


@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderQueryService orderQueryService;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CoinService coinService;
    private final UserCoinService userCoinService;
    private final TimeProvider timeProvider;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public OrderCreateResponse createLimitBuyOrder(LimitOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime registeredDateTime = timeProvider.now();

        Order order = orderRepository.save(buildLimitOrder(request, BUY, user, registeredDateTime));

        outboxEventPublisher.publish(
                LIMIT_BUY_ORDER_CREATE,
                EventPayload.builder()
                        .orderId(order.getId())
                        .userId(order.getUser().getId())
                        .symbol(order.getCoin().getSymbol())
                        .price(order.getPrice())
                        .quantity(order.getQuantity())
                        .orderSide(order.getOrderSide().name())
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
                EventPayload.builder()
                        .orderId(order.getId())
                        .userId(order.getUser().getId())
                        .symbol(order.getCoin().getSymbol())
                        .price(order.getPrice())
                        .quantity(order.getQuantity())
                        .orderSide(order.getOrderSide().name())
                        .build(),
                order.getId()
        );

        return OrderCreateResponse.of(order);
    }

    @Transactional
    public OrderCreateResponse createMarketBuyOrder(MarketBuyOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime registeredDateTime = timeProvider.now();
        BigDecimal totalPrice = request.getTotalPrice();
        Coin coin = coinService.getCoinOrThrow(request.getSymbol());

        Order order = orderRepository.save(Order.createMarketBuyOrder(totalPrice, coin, user, registeredDateTime));

        outboxEventPublisher.publish(
                MARKET_BUY_ORDER_CREATE,
                EventPayload.builder()
                        .orderId(order.getId())
                        .symbol(order.getCoin().getSymbol())
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
                EventPayload.builder()
                        .orderId(order.getId())
                        .symbol(order.getCoin().getSymbol())
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
}
