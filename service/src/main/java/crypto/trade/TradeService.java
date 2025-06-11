package crypto.trade;

import crypto.event.Event;
import crypto.event.EventPayload;
import crypto.fee.FeePolicy;
import crypto.order.*;
import crypto.trade.eventhandler.EventHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderRole.MAKER;
import static crypto.order.OrderRole.TAKER;
import static crypto.order.OrderSide.BUY;


@RequiredArgsConstructor
@Service
public class TradeService {

    private final List<EventHandler> eventHandlers;
    private final TradeSettlementService tradeSettlementService;
    private final TradeRepository tradeRepository;
    private final FeePolicy feePolicy;


    public void handleEvent(Event<EventPayload> event) {
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }
        eventHandler.handle(event);
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    public void processMatchLimitOrder(Order matchOrder, Order placeOrder, OrderSide orderSide, LocalDateTime registeredDateTime) {
        BigDecimal matchOrderQuantity = matchOrder.calculateRemainQuantity();
        BigDecimal placeOrderQuantity = placeOrder.calculateRemainQuantity();

        BigDecimal matchedQty = matchOrderQuantity.min(placeOrderQuantity);
        BigDecimal matchedPrice = placeOrder.getPrice();
        BigDecimal totalPrice = matchedPrice.multiply(matchedQty);

        BigDecimal takerFee = calculateTradeFee(totalPrice, TAKER);
        BigDecimal makerFee = calculateTradeFee(totalPrice, MAKER);

        BigDecimal takerTotalUsed = totalPrice.add(takerFee);
        BigDecimal makerTotalUsed = totalPrice.add(makerFee);

        matchOrder.fill(matchedQty);
        placeOrder.fill(matchedQty);

        Trade trade = createAndSaveTrade(matchOrder, placeOrder, matchedPrice, matchedQty, orderSide, takerFee, makerFee, registeredDateTime);

        if (orderSide.equals(BUY)) {
            tradeSettlementService.buyOrderSettle(takerTotalUsed, makerTotalUsed, trade, matchOrder, placeOrder);
        } else {
            tradeSettlementService.sellOrderSettle(takerTotalUsed, makerTotalUsed, trade, matchOrder, placeOrder);
        }
    }

    public BigDecimal calculateTradeFee(BigDecimal amount, OrderRole role) {
        BigDecimal feeRate = (role == MAKER)
                ? feePolicy.getMakerFeeRate()
                : feePolicy.getTakerFeeRate();

        return amount.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }

    public Trade createAndSaveTrade(Order matchOrder, Order placeOrder, BigDecimal price, BigDecimal qty,
                                     OrderSide takerSide, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime registeredDateTime) {
        return tradeRepository.save(Trade.create(
                matchOrder.getCoin().getSymbol(), price, qty, takerSide,
                matchOrder.getId(), placeOrder.getId(),
                matchOrder.getUser().getId(), placeOrder.getUser().getId(),
                takerFee, makerFee, registeredDateTime
        ));
    }

    public void settleAndMarkOrders(Order matchOrder, Order placeOrder, BigDecimal matchedQty, BigDecimal takerTotalUsed,
                                     BigDecimal makerTotalUsed, Trade trade, OrderSide orderSide) {
        matchOrder.fill(matchedQty);
        placeOrder.fill(matchedQty);

        if (orderSide.equals(BUY)) {
            tradeSettlementService.buyOrderSettle(takerTotalUsed, makerTotalUsed, trade, matchOrder, placeOrder);
        } else {
            tradeSettlementService.sellOrderSettle(takerTotalUsed, makerTotalUsed, trade, matchOrder, placeOrder);
        }

        if (placeOrder.isFullyFilled()) placeOrder.markCompleted();
    }
}
