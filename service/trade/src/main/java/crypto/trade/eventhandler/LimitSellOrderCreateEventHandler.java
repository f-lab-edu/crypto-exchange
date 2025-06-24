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
public class LimitSellOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        EventPayload payload = event.getPayload();

        TradeOrder sellOrder = tradeOrderRepository.findById(payload.getOrderId())
                .orElseThrow(TradeOrderNotFoundException::new);

        List<TradeOrder> buyOrders = tradeOrderRepository.findMatchedLimitSellOrders(sellOrder.getSymbol(), sellOrder.getOrderSide(), sellOrder.getPrice());

        if (buyOrders.isEmpty()) {
            return;
        }

        for (TradeOrder buyOrder : buyOrders) {
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
    public EventType getSupportedEventType() {
        return EventType.LIMIT_SELL_ORDER_CREATE;
    }
}
