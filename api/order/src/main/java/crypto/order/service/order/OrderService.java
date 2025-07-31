package crypto.order.service.order;

import crypto.common.security.context.UserContext;
import crypto.common.time.TimeProvider;
import crypto.event.eventsender.OrderEventSender;
import crypto.event.payload.EventPayload;
import crypto.order.controller.order.response.*;
import crypto.order.service.order.request.LimitOrderServiceRequest;
import crypto.order.service.order.request.MarketBuyOrderServiceRequest;
import crypto.order.service.order.request.MarketSellOrderServiceRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static crypto.event.EventType.*;
import static crypto.order.entity.order.OrderSide.*;



@RequiredArgsConstructor
@Service
public class OrderService {

    private final TimeProvider timeProvider;
    private final OrderEventSender orderEventSender;

    public OrderCreateResponse createLimitBuyOrder(LimitOrderServiceRequest request) {
        Long userId = UserContext.getUserId();
        LocalDateTime createdAt = timeProvider.now();

        orderEventSender.send(
                LIMIT_BUY_ORDER_CREATE,
                EventPayload.builder()
                        .userId(userId)
                        .symbol(request.getSymbol())
                        .price(request.getPrice())
                        .quantity(request.getQuantity())
                        .orderSide(BUY.name())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    public OrderCreateResponse createLimitSellOrder(LimitOrderServiceRequest request) {
        Long userId = UserContext.getUserId();
        LocalDateTime createdAt = timeProvider.now();

        orderEventSender.send(
                LIMIT_SELL_ORDER_CREATE,
                EventPayload.builder()
                        .userId(userId)
                        .symbol(request.getSymbol())
                        .price(request.getPrice())
                        .quantity(request.getQuantity())
                        .orderSide(SELL.name())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    public OrderCreateResponse createMarketBuyOrder(MarketBuyOrderServiceRequest request) {
        Long userId = UserContext.getUserId();
        LocalDateTime createdAt = timeProvider.now();

        orderEventSender.send(
                MARKET_BUY_ORDER_CREATE,
                EventPayload.builder()
                        .userId(userId)
                        .symbol(request.getSymbol())
                        .marketTotalPrice(request.getTotalPrice())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

    public OrderCreateResponse createMarketSellOrder(MarketSellOrderServiceRequest request) {
        Long userId = UserContext.getUserId();
        LocalDateTime createdAt = timeProvider.now();

        orderEventSender.send(
                MARKET_SELL_ORDER_CREATE,
                EventPayload.builder()
                        .userId(userId)
                        .symbol(request.getSymbol())
                        .marketTotalQuantity(request.getTotalAmount())
                        .build()
        );

        return OrderCreateResponse.of(createdAt);
    }

//    @Transactional
//    public OrderDeleteResponse deleteOrder(Long orderId) {
//        LocalDateTime deletedDateTime = timeProvider.now();
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(OrderNotFoundException::new);
//        order.markDeleted(deletedDateTime);
//
//        return OrderDeleteResponse.of(order);
//    }
}
