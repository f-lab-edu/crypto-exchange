package crypto.trade.eventhandler;

import crypto.common.fee.FeePolicy;
import crypto.event.eventsender.SettlementEventSender;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.Trade;
import crypto.trade.entity.TradeOrder;
import crypto.trade.entity.TradeOrderRole;
import crypto.trade.entity.TradeOrderSide;
import crypto.trade.entity.exception.FilledQuantityExceedException;
import crypto.trade.eventhandler.exception.OrderFillException;
import crypto.trade.repository.TradeOrderRepository;
import crypto.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static crypto.event.EventType.*;
import static crypto.trade.entity.TradeOrderRole.*;
import static crypto.trade.entity.TradeOrderSide.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class TradeProcessor {
    private final SettlementEventSender settlementEventSender;
    private final TradeRepository tradeRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final FeePolicy feePolicy;

    public void processMatchLimitOrder(Long orderId, TradeOrder matchOrder, TradeOrder placeOrder, TradeOrderSide orderSide, LocalDateTime registeredDateTime) {
        try {
            BigDecimal matchOrderQuantity = matchOrder.calculateRemainQuantity();
            BigDecimal placeOrderQuantity = placeOrder.calculateRemainQuantity();

            BigDecimal matchedQty = matchOrderQuantity.min(placeOrderQuantity);
            BigDecimal matchedPrice = placeOrder.getPrice();
            BigDecimal totalPrice = matchedPrice.multiply(matchedQty);

            BigDecimal takerFee = calculateTradeFee(totalPrice, TAKER);
            BigDecimal makerFee = calculateTradeFee(totalPrice, MAKER);

            BigDecimal takerTotalUsed = totalPrice.add(takerFee);
            BigDecimal makerTotalUsed = totalPrice.add(makerFee);

            int makerUpdatedRows = tradeOrderRepository.fillAtomically(matchOrder.getId(), matchedQty);

            if (makerUpdatedRows == 0) {
                throw new OrderFillException();
            }

            int takerUpdatedRows = tradeOrderRepository.fillAtomically(placeOrder.getId(), matchedQty);

            if (takerUpdatedRows == 0) {
                throw new OrderFillException();
            }

            Trade trade = createAndSaveTradeLimitOrder(matchOrder, placeOrder, matchedPrice, matchedQty, orderSide, takerFee, makerFee, registeredDateTime);

            if (orderSide.equals(BUY)) {
                settlementEventSender.send(
                        BUY_ORDER_SETTLEMENT,
                        matchOrder.getId(),
                        EventPayload.builder()
                                .orderId(orderId)
                                .tradeId(trade.getId())
                                .takerOrderId(matchOrder.getId())
                                .makerOrderId(placeOrder.getId())
                                .takerId(matchOrder.getUserId())
                                .makerId(placeOrder.getUserId())
                                .takerTotalUsed(takerTotalUsed)
                                .makerTotalUsed(makerTotalUsed)
                                .matchedQuantity(matchedQty)
                                .symbol(matchOrder.getSymbol())
                                .build()
                );
            } else {
                settlementEventSender.send(
                        SELL_ORDER_SETTLEMENT,
                        matchOrder.getId(),
                        EventPayload.builder()
                                .orderId(orderId)
                                .tradeId(trade.getId())
                                .takerOrderId(matchOrder.getId())
                                .makerOrderId(placeOrder.getId())
                                .takerId(matchOrder.getUserId())
                                .makerId(placeOrder.getUserId())
                                .takerTotalUsed(takerTotalUsed)
                                .makerTotalUsed(makerTotalUsed)
                                .matchedQuantity(matchedQty)
                                .symbol(matchOrder.getSymbol())
                                .build()
                );
            }
        } catch (FilledQuantityExceedException e) {
            log.error("[TradeProcessor.processMatchLimitOrder] Filled quantity exceeded. matchOrderId={}, placeOrderId={}",
                    matchOrder.getId(), placeOrder.getId(), e);
        } catch (Exception e) {
            log.error("[TradeProcessor.processMatchLimitOrder] Unexpected error during order match. matchOrderId={}, placeOrderId={}",
                    matchOrder.getId(), placeOrder.getId(), e);
        }
    }

    public void settleAndMarkOrders(Long orderId, Long tradeId, Long takerId, Long makerId, TradeOrder placeOrder, BigDecimal matchedQty, BigDecimal takerTotalUsed,
                                    BigDecimal makerTotalUsed, TradeOrderSide orderSide) {
        placeOrder.fill(matchedQty);

        if (orderSide.equals(BUY)) {
            settlementEventSender.send(
                    BUY_ORDER_SETTLEMENT,
                    placeOrder.getId(),
                    EventPayload.builder()
                            .orderId(orderId)
                            .tradeId(tradeId)
                            .takerId(takerId)
                            .makerId(makerId)
                            .takerTotalUsed(takerTotalUsed)
                            .makerTotalUsed(makerTotalUsed)
                            .matchedQuantity(matchedQty)
                            .symbol(placeOrder.getSymbol())
                            .build()
            );
        } else {
            settlementEventSender.send(
                    SELL_ORDER_SETTLEMENT,
                    placeOrder.getId(),
                    EventPayload.builder()
                            .orderId(orderId)
                            .tradeId(tradeId)
                            .takerId(takerId)
                            .makerId(makerId)
                            .takerTotalUsed(takerTotalUsed)
                            .makerTotalUsed(makerTotalUsed)
                            .matchedQuantity(matchedQty)
                            .symbol(placeOrder.getSymbol())
                            .build()
            );
        }

        if (placeOrder.isFullyFilled()) placeOrder.markCompleted();
    }

    public void refundUnmatchedLockedBalance(Long userId, BigDecimal remainPrice) {
        if (remainPrice.compareTo(BigDecimal.ZERO) > 0) {
            settlementEventSender.send(
                    REFUND_LOCKED_BALANCE,
                    userId,
                    EventPayload.builder()
                            .userId(userId)
                            .totalRemainPrice(remainPrice)
                            .build()
            );
        }
    }

    public BigDecimal calculateTradeFee(BigDecimal amount, TradeOrderRole role) {
        BigDecimal feeRate = (role == MAKER)
                ? feePolicy.getMakerFeeRate()
                : feePolicy.getTakerFeeRate();

        return amount.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }

    public Trade createAndSaveTradeLimitOrder(TradeOrder matchOrder, TradeOrder placeOrder, BigDecimal price, BigDecimal qty,
                                    TradeOrderSide takerSide, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime registeredDateTime) {

        return tradeRepository.save(Trade.create(
                matchOrder.getSymbol(), price, qty, takerSide.name(),
                matchOrder.getId(), placeOrder.getId(),
                matchOrder.getUserId(), placeOrder.getUserId(),
                takerFee, makerFee, registeredDateTime
        ));
    }

    public Trade createAndSaveTradeMarketOrder(TradeOrder placeOrder, BigDecimal price, BigDecimal qty,
                                    TradeOrderSide takerSide, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime registeredDateTime) {

        return tradeRepository.save(Trade.create(
                placeOrder.getSymbol(), price, qty, takerSide.name(),
                placeOrder.getId(), placeOrder.getId(),
                placeOrder.getUserId(), placeOrder.getUserId(),
                takerFee, makerFee, registeredDateTime
        ));
    }
}
