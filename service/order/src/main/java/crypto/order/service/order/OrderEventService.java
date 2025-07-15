package crypto.order.service.order;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.eventsender.TradeEventSender;
import crypto.event.payload.EventPayload;
import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderProcessedEvent;
import crypto.order.entity.order.OrderSide;
import crypto.order.entity.order.exception.OrderNotFoundException;
import crypto.order.entity.user.User;
import crypto.order.repository.order.OrderProcessedEventDbRepository;
import crypto.order.repository.order.OrderProcessedEventRepository;
import crypto.order.repository.order.OrderRepository;
import crypto.order.service.coin.CoinService;
import crypto.order.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static crypto.event.EventType.*;
import static crypto.event.EventType.MARKET_SELL_ORDER_CREATE;
import static crypto.order.entity.order.OrderStatus.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventService {

    private final TimeProvider timeProvider;
    private final OrderProcessedEventRepository orderProcessedEventRepository;
    private final OrderProcessedEventDbRepository orderProcessedEventDbRepository;
    private final OrderRepository orderRepository;
    private final TradeEventSender tradeEventSender;
    private final CoinService coinService;
    private final UserService userService;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();
        Boolean isNewEvent = orderProcessedEventRepository.setIfAbsent(eventId);

        if (!isNewEvent) {
            log.warn("[OrderEventService.handleEvent] Duplicate event detected, skipping processing. eventId={}", eventId);
            return;
        }

        try {
            EventPayload payload = event.getPayload();
            LocalDateTime registeredDateTime = timeProvider.now();
            Coin coin = coinService.getCoinOrThrow(payload.getSymbol());
            User user = userService.getUserOrThrow(payload.getUserId());
            Order order = createOrderFromEvent(event, coin, user, registeredDateTime);

            if (order != null) {
                orderRepository.save(order);

                EventPayload tradePayload = createTradePayload(order, payload);
                tradeEventSender.send(event.getType().toMatchingEventType(), order.getId(), tradePayload);

                orderProcessedEventDbRepository.save(new OrderProcessedEvent(eventId));
                log.info("[OrderEventConsumer.handleEvent] Order saved successfully: {}", order);

            } else {
                throw new IllegalArgumentException("Unknown event type received: " + event.getType());
            }

        } catch (Exception e) {
            orderProcessedEventRepository.delete(orderProcessedEventRepository.generateKey(eventId));
            throw new RuntimeException("Failed to process order creation for eventId: " + eventId, e);
        }
    }

    @Transactional
    public void handleFailEvent(Event event) {
        String eventId = event.getEventId();
        Boolean isNewEvent = orderProcessedEventRepository.setIfAbsent(eventId);

        if (!isNewEvent) {
            log.warn("[OrderEventService.handleFailEvent] Duplicate event detected, skipping processing. eventId={}", eventId);
            return;
        }

        try {
            EventPayload payload = event.getPayload();
            Order order = orderRepository.findById(payload.getOrderId())
                    .orElseThrow(OrderNotFoundException::new);

            order.handleOrderStatus(CANCELLED);
            orderRepository.save(order);

            orderProcessedEventDbRepository.save(new OrderProcessedEvent(eventId));
            log.info("[OrderEventConsumer.handleFailEvent] Order cancellation successfully: {}", order);

        } catch (Exception e) {
            orderProcessedEventRepository.delete(orderProcessedEventRepository.generateKey(eventId));
            throw new RuntimeException("Failed to process order creation for eventId: " + eventId, e);
        }
    }

    private Order createOrderFromEvent(Event event, Coin coin, User user, LocalDateTime registeredDateTime) {
        EventPayload payload = event.getPayload();
        Order order;

        if (event.getType() == LIMIT_BUY_ORDER_CREATE || event.getType() == LIMIT_SELL_ORDER_CREATE) {
            order = Order.createLimitOrder(
                    payload.getPrice(),
                    payload.getQuantity(),
                    OrderSide.valueOf(payload.getOrderSide()),
                    coin,
                    user,
                    registeredDateTime
            );
        } else if (event.getType() == MARKET_BUY_ORDER_CREATE) {
            order = Order.createMarketBuyOrder(
                    payload.getMarketTotalPrice(),
                    coin,
                    user,
                    registeredDateTime
            );
        } else if (event.getType() == MARKET_SELL_ORDER_CREATE) {
            order = Order.createMarketSellOrder(
                    payload.getMarketTotalQuantity(),
                    coin,
                    user,
                    registeredDateTime
            );
        } else {
            throw new IllegalArgumentException("Unknown or unsupported event type for order creation: " + event.getType());
        }

        return order;
    }

    private EventPayload createTradePayload(Order order, EventPayload payload) {
        return EventPayload.builder()
                .orderId(order.getId())
                .userId(payload.getUserId())
                .symbol(payload.getSymbol())
                .price(payload.getPrice())
                .quantity(payload.getQuantity())
                .orderSide(payload.getOrderSide())
                .build();
    }
}
