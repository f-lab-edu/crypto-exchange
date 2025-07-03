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
import java.time.LocalDateTime;
import java.util.List;

import static crypto.trade.entity.TradeOrderRole.*;
import static crypto.trade.entity.TradeOrderSide.*;


@Component
@RequiredArgsConstructor
public class MarketSellOrderCreateEventHandler implements EventHandler {
    private final TradeProcessor tradeProcessor;
    private final TradeOrderRepository tradeOrderRepository;
    private final TimeProvider timeProvider;

    @Override
    public void handle(Event event) {
        LocalDateTime registeredDateTime = timeProvider.now();
        EventPayload payload = event.getPayload();

        TradeOrder sellOrder = tradeOrderRepository.findById(payload.getOrderId())
                .orElseThrow(TradeOrderNotFoundException::new);

        List<TradeOrder> buyOrders = tradeOrderRepository.findMatchedMarketSellOrders(sellOrder.getSymbol(), sellOrder.getOrderSide());

        BigDecimal remainQty = payload.getMarketTotalQuantity();

        for (TradeOrder buyOrder : buyOrders) {
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
            tradeProcessor.settleAndMarkOrders(sellOrder, buyOrder, matchedQty, takerTotalUsed, makerTotalUsed, SELL);
        }
    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.MARKET_SELL_ORDER_TRADE;
    }
}
