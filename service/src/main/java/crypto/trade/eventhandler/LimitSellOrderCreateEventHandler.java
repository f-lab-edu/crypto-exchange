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

import static crypto.order.OrderSide.SELL;


@Component
@RequiredArgsConstructor
public class LimitSellOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final OrderQueryService orderQueryService;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        UnifiedEventPayload payload = event.getPayload();

        Order sellOrder = orderQueryService.findOrder(payload.getOrderId());
        List<Order> buyOrders = orderQueryService.getMatchedLimitSellOrders(sellOrder.getCoin(), SELL, sellOrder.getPrice());

        if (buyOrders.isEmpty()) {
            return;
        }

        for (Order buyOrder : buyOrders) {
            tradeProcessor.processMatchLimitOrder(sellOrder, buyOrder, SELL, registeredDateTime);

            if (buyOrder.isFullyFilled()) {
                sellOrder.markCompleted();
            }

            if (sellOrder.isFullyFilled()) {
                buyOrder.markCompleted();
                break;
            }
        }
    }

    @Override
    public boolean supports(Event event) {
        return EventType.LIMIT_SELL_ORDER_CREATE == event.getType();
    }
}
