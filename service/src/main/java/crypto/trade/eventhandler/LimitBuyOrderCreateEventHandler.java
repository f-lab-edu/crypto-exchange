package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.LimitOrderCreateEventPayload;
import crypto.order.Order;
import crypto.order.OrderQueryService;
import crypto.order.OrderService;
import crypto.time.TimeProvider;

import crypto.trade.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderSide.BUY;
import static crypto.order.OrderSide.SELL;


@Component
@RequiredArgsConstructor
public class LimitBuyOrderCreateEventHandler implements EventHandler<LimitOrderCreateEventPayload> {
    private final TradeService tradeService;
    private final OrderService orderService;
    private final OrderQueryService orderQueryService;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event<LimitOrderCreateEventPayload> event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        LimitOrderCreateEventPayload payload = event.getPayload();

        Order buyOrder = orderService.findOrder(payload.getOrderId());
        List<Order> sellOrders = orderQueryService.getMatchedLimitBuyOrders(buyOrder.getCoin(), SELL, buyOrder.getPrice());

        if (sellOrders.isEmpty()) {
            return;
        }

        for (Order sellOrder : sellOrders) {
            tradeService.processMatchLimitOrder(buyOrder, sellOrder, BUY, registeredDateTime);

            if (sellOrder.isFullyFilled()) {
                sellOrder.markCompleted();
            }

            if (buyOrder.isFullyFilled()) {
                buyOrder.markCompleted();
                break;
            }
        }
    }

    @Override
    public boolean supports(Event<LimitOrderCreateEventPayload> event) {
        return EventType.LIMIT_BUY_ORDER_CREATE == event.getType();
    }
}
