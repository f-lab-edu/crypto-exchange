package crypto.trade.eventhandler;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.EventType;
import crypto.trade.entity.TradeOrder;
import crypto.trade.repository.TradeOrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static crypto.trade.entity.TradeOrderSide.*;


@Component
@RequiredArgsConstructor
public class LimitBuyOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event, TradeOrder buyOrder) {
        LocalDateTime registeredDateTime = timeProvider.now();
        Long orderId = event.getPayload().getOrderId();

        List<TradeOrder> sellOrders = tradeOrderRepository.findMatchedLimitBuyOrders(buyOrder.getSymbol(), SELL, buyOrder.getPrice());

        if (sellOrders.isEmpty()) {
            return;
        }

        for (TradeOrder sellOrder : sellOrders) {
            tradeProcessor.processMatchLimitOrder(orderId, buyOrder, sellOrder, BUY, registeredDateTime);

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
    public EventType getSupportedEventType() {
        return EventType.LIMIT_BUY_ORDER_TRADE;
    }
}
