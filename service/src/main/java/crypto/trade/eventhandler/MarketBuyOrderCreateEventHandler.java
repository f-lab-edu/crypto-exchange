package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.MarketBuyOrderCreateEventPayload;
import crypto.order.Order;
import crypto.order.OrderQueryService;
import crypto.time.TimeProvider;
import crypto.trade.Trade;

import crypto.trade.TradeProcessor;
import crypto.trade.TradeSettlementService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderRole.MAKER;
import static crypto.order.OrderRole.TAKER;
import static crypto.order.OrderSide.BUY;
import static crypto.order.OrderSide.SELL;


@Component
@RequiredArgsConstructor
public class MarketBuyOrderCreateEventHandler implements EventHandler<MarketBuyOrderCreateEventPayload> {
    private final TradeProcessor tradeProcessor;
    private final OrderQueryService orderQueryService;
    private final TradeSettlementService tradeSettlementService;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event<MarketBuyOrderCreateEventPayload> event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        MarketBuyOrderCreateEventPayload payload = event.getPayload();

        Order buyOrder = orderQueryService.findOrder(payload.getOrderId());
        List<Order> sellOrders = orderQueryService.getMatchedMarketBuyOrders(buyOrder.getCoin(), SELL);

        BigDecimal remainPrice = buyOrder.getMarKetTotalPrice();

        for (Order sellOrder : sellOrders) {
            BigDecimal sellPrice = sellOrder.getPrice();
            BigDecimal sellRemainQty = sellOrder.calculateRemainQuantity();
            BigDecimal maxBuyQty = remainPrice.divide(sellPrice, 8, RoundingMode.DOWN);
            BigDecimal matchedQty = maxBuyQty.min(sellRemainQty);

            if (matchedQty.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal matchedAmount = sellPrice.multiply(matchedQty);
            BigDecimal takerFee = tradeProcessor.calculateTradeFee(matchedAmount, TAKER);
            BigDecimal makerFee = tradeProcessor.calculateTradeFee(matchedAmount, MAKER);
            BigDecimal takerTotalUsed = matchedAmount.add(takerFee);
            BigDecimal makerTotalUsed = matchedAmount.add(makerFee);

            if (takerTotalUsed.compareTo(remainPrice) > 0) break;

            Trade trade = tradeProcessor.createAndSaveTrade(buyOrder, sellOrder, sellPrice, matchedQty, BUY, takerFee, makerFee, registeredDateTime);
            tradeProcessor.settleAndMarkOrders(buyOrder, sellOrder, matchedQty, takerTotalUsed, makerTotalUsed, trade, BUY);
            remainPrice = remainPrice.subtract(takerTotalUsed);
        }

        if (remainPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalPrice = remainPrice.add(tradeProcessor.calculateTradeFee(buyOrder.getMarKetTotalPrice(), TAKER));
            tradeSettlementService.refundUnmatchedLockedBalance(buyOrder, totalPrice);
        }

    }

    @Override
    public boolean supports(Event<MarketBuyOrderCreateEventPayload> event) {
        return EventType.MARKET_BUY_ORDER_CREATE == event.getType();
    }
}
