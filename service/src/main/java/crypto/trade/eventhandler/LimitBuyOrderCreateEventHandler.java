package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.UnifiedEventPayload;
import crypto.order.Order;
import crypto.order.OrderQueryService;
import crypto.time.TimeProvider;
import crypto.trade.TradeProcessor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderSide.BUY;
import static crypto.order.OrderSide.SELL;


@Component
@RequiredArgsConstructor
public class LimitBuyOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final OrderQueryService orderQueryService;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        UnifiedEventPayload payload = event.getPayload();

        Order buyOrder = orderQueryService.findOrder(payload.getOrderId());
        List<Order> sellOrders = orderQueryService.getMatchedLimitBuyOrders(buyOrder.getCoin(), SELL, buyOrder.getPrice());

        if (sellOrders.isEmpty()) {
            return;
        }

        for (Order sellOrder : sellOrders) {
            tradeProcessor.processMatchLimitOrder(buyOrder, sellOrder, BUY, registeredDateTime);

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
    public boolean supports(Event event) {
        return EventType.LIMIT_BUY_ORDER_CREATE == event.getType();
    }
}
