package crypto.trade.eventhandler;

import crypto.common.time.TimeProvider;
import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.Trade;
import crypto.trade.entity.TradeOrder;
import crypto.trade.eventhandler.exception.TradeOrderNotFoundException;
import crypto.trade.repository.TradeOrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.trade.entity.TradeOrderRole.*;
import static crypto.trade.entity.TradeOrderSide.*;


@Component
@RequiredArgsConstructor
public class MarketBuyOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        EventPayload payload = event.getPayload();

        TradeOrder buyOrder = tradeOrderRepository.findById(payload.getOrderId())
                .orElseThrow(TradeOrderNotFoundException::new);

        List<TradeOrder> sellOrders = tradeOrderRepository.findMatchedMarketBuyOrders(buyOrder.getSymbol(), buyOrder.getOrderSide());

        BigDecimal remainPrice = payload.getMarketTotalPrice();

        for (TradeOrder sellOrder : sellOrders) {
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
            BigDecimal totalPrice = remainPrice.add(tradeProcessor.calculateTradeFee(payload.getMarketTotalPrice(), TAKER));
            tradeProcessor.refundUnmatchedLockedBalance(buyOrder, totalPrice);
        }

    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.MARKET_BUY_ORDER_CREATE;
    }
}
