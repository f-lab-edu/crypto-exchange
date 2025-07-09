package crypto.trade.eventhandler;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.TradeOrder;
import crypto.trade.eventhandler.exception.TradeOrderNotFoundException;
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
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        EventPayload payload = event.getPayload();

        TradeOrder buyOrder = tradeOrderRepository.findById(payload.getOrderId())
                .orElseThrow(TradeOrderNotFoundException::new);

        List<TradeOrder> sellOrders = tradeOrderRepository.findMatchedLimitBuyOrders(buyOrder.getSymbol(), buyOrder.getOrderSide(), buyOrder.getPrice());

        if (sellOrders.isEmpty()) {
            return;
        }

        for (TradeOrder sellOrder : sellOrders) {
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
    public EventType getSupportedEventType() {
        return EventType.LIMIT_BUY_ORDER_TRADE;
    }
}
