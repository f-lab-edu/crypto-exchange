package crypto.order.service.order;

import crypto.common.time.TimeProvider;
import crypto.event.EventSender;
import crypto.event.payload.EventPayload;
import crypto.order.controller.response.*;
import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import crypto.order.entity.user.User;
import crypto.order.repository.order.OrderRepository;
import crypto.order.service.order.exception.OrderNotFoundException;
import crypto.order.service.order.request.LimitOrderServiceRequest;
import crypto.order.service.order.request.MarketBuyOrderServiceRequest;
import crypto.order.service.order.request.MarketSellOrderServiceRequest;
import crypto.order.service.user.UserService;

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

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final TimeProvider timeProvider;
    private final EventSender eventSender;

    @Transactional
    public OrderCreateResponse createLimitBuyOrder(LimitOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime createdAt = timeProvider.now();

        eventSender.publish(
                LIMIT_BUY_ORDER_CREATE,
                EventPayload.builder()
                        .userId(user.getId())
                        .symbol(request.getSymbol())
                        .price(request.getPrice())
                        .quantity(request.getQuantity())
                        .orderSide(BUY.name())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    @Transactional
    public OrderCreateResponse createLimitSellOrder(LimitOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime createdAt = timeProvider.now();

        eventSender.publish(
                LIMIT_SELL_ORDER_CREATE,
                EventPayload.builder()
                        .userId(user.getId())
                        .symbol(request.getSymbol())
                        .price(request.getPrice())
                        .quantity(request.getQuantity())
                        .orderSide(SELL.name())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    @Transactional
    public OrderCreateResponse createMarketBuyOrder(MarketBuyOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime createdAt = timeProvider.now();

        eventSender.publish(
                MARKET_BUY_ORDER_CREATE,
                EventPayload.builder()
                        .symbol(request.getSymbol())
                        .marketTotalPrice(request.getTotalPrice())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    @Transactional
    public OrderCreateResponse createMarketSellOrder(MarketSellOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime createdAt = timeProvider.now();

        eventSender.publish(
                MARKET_SELL_ORDER_CREATE,
                EventPayload.builder()
                        .symbol(request.getSymbol())
                        .marketTotalQuantity(request.getTotalAmount())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    @Transactional
    public OrderDeleteResponse deleteOrder(Long orderId) {
        LocalDateTime deletedDateTime = timeProvider.now();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
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
