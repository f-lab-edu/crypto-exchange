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
public class LimitSellOrderCreateEventHandler implements EventHandler<LimitOrderCreateEventPayload> {
    private final TradeService tradeService;
    private final OrderService orderService;
    private final OrderQueryService orderQueryService;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event<LimitOrderCreateEventPayload> event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        LimitOrderCreateEventPayload payload = event.getPayload();

        Order sellOrder = orderService.findOrder(payload.getOrderId());
        List<Order> buyOrders = orderQueryService.getMatchedLimitSellOrders(sellOrder.getCoin(), SELL, sellOrder.getPrice());

        if (buyOrders.isEmpty()) {
            return;
        }

        for (Order buyOrder : buyOrders) {
            tradeService.processMatchLimitOrder(sellOrder, buyOrder, SELL, registeredDateTime);

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
    public boolean supports(Event<LimitOrderCreateEventPayload> event) {
        return EventType.LIMIT_SELL_ORDER_CREATE == event.getType();
    }
}
