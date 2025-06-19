package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.UnifiedEventPayload;
import crypto.order.Order;
import crypto.order.OrderQueryService;
import crypto.time.TimeProvider;
import crypto.trade.Trade;
import crypto.trade.TradeProcessor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderRole.MAKER;
import static crypto.order.OrderRole.TAKER;
import static crypto.order.OrderSide.BUY;
import static crypto.order.OrderSide.SELL;


@Component
@RequiredArgsConstructor
public class MarketSellOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final OrderQueryService orderQueryService;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        UnifiedEventPayload payload = event.getPayload();

        Order sellOrder = orderQueryService.findOrder(payload.getOrderId());
        List<Order> buyOrders = orderQueryService.getMatchedMarketBuyOrders(sellOrder.getCoin(), BUY);

        BigDecimal remainQty = sellOrder.getMarketTotalQuantity();

        for (Order buyOrder : buyOrders) {
            BigDecimal buyPrice = buyOrder.getPrice();
            BigDecimal buyRemainQty = buyOrder.calculateRemainQuantity();
            BigDecimal matchedQty = remainQty.min(buyRemainQty);

            if (matchedQty.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal matchedAmount = buyPrice.multiply(matchedQty);
            BigDecimal takerFee = tradeProcessor.calculateTradeFee(matchedAmount, TAKER);
            BigDecimal makerFee = tradeProcessor.calculateTradeFee(matchedAmount, MAKER);
            BigDecimal takerTotalUsed = matchedAmount.add(takerFee);
            BigDecimal makerTotalUsed = matchedAmount.add(makerFee);

            Trade trade = tradeProcessor.createAndSaveTrade(sellOrder, buyOrder, buyPrice, matchedQty, SELL, takerFee, makerFee, registeredDateTime);
            remainQty = remainQty.subtract(matchedQty);
            tradeProcessor.settleAndMarkOrders(sellOrder, buyOrder, matchedQty, takerTotalUsed, makerTotalUsed, trade, SELL);
        }
    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.MARKET_SELL_ORDER_CREATE;
    }
}
