package crypto.trade.eventhandler;

import crypto.common.fee.FeePolicy;
import crypto.event.payload.EventPayload;
import crypto.trade.entity.Trade;
import crypto.trade.entity.TradeOrder;
import crypto.trade.entity.TradeOrderRole;
import crypto.trade.entity.TradeOrderSide;
import crypto.trade.entity.exception.FilledQuantityExceedException;
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
    // private final EventSender outboxEventPublisher;
    private final TradeRepository tradeRepository;
    private final FeePolicy feePolicy;

    public void processMatchLimitOrder(TradeOrder matchOrder, TradeOrder placeOrder, TradeOrderSide orderSide, LocalDateTime registeredDateTime) {
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

            matchOrder.fill(matchedQty);
            placeOrder.fill(matchedQty);

            Trade trade = createAndSaveTrade(matchOrder, placeOrder, matchedPrice, matchedQty, orderSide, takerFee, makerFee, registeredDateTime);

            if (orderSide.equals(BUY)) {
                outboxEventPublisher.publish(
                        TRADE_BUY_ORDER_CREATE,
                        EventPayload.builder()
                                .takerId(matchOrder.getUserId())
                                .makerId(placeOrder.getUserId())
                                .takerTotalUsed(takerTotalUsed)
                                .makerTotalUsed(makerTotalUsed)
                                .matchedQuantity(matchedQty)
                                .symbol(matchOrder.getSymbol())
                                .build(),
                        trade.getId()
                );
            } else {
                outboxEventPublisher.publish(
                        TRADE_SELL_ORDER_CREATE,
                        EventPayload.builder()
                                .takerId(matchOrder.getUserId())
                                .makerId(placeOrder.getUserId())
                                .takerTotalUsed(takerTotalUsed)
                                .makerTotalUsed(makerTotalUsed)
                                .matchedQuantity(matchedQty)
                                .symbol(matchOrder.getSymbol())
                                .build(),
                        trade.getId()
                );
            }
        } catch (FilledQuantityExceedException e) {
            log.error("[TradeProcessor] Filled quantity exceeded. matchOrderId={}, placeOrderId={}",
                    matchOrder.getId(), placeOrder.getId(), e);
        } catch (Exception e) {
            log.error("[TradeProcessor] Unexpected error during order match. matchOrderId={}, placeOrderId={}",
                    matchOrder.getId(), placeOrder.getId(), e);
        }
    }

    public void settleAndMarkOrders(TradeOrder matchOrder, TradeOrder placeOrder, BigDecimal matchedQty, BigDecimal takerTotalUsed,
                                    BigDecimal makerTotalUsed, Trade trade, TradeOrderSide orderSide) {
        matchOrder.fill(matchedQty);
        placeOrder.fill(matchedQty);

        if (orderSide.equals(BUY)) {
            outboxEventPublisher.publish(
                    TRADE_BUY_ORDER_CREATE,
                    EventPayload.builder()
                            .takerId(matchOrder.getUserId())
                            .makerId(placeOrder.getUserId())
                            .takerTotalUsed(takerTotalUsed)
                            .makerTotalUsed(makerTotalUsed)
                            .matchedQuantity(matchedQty)
                            .symbol(matchOrder.getSymbol())
                            .build(),
                    trade.getId()
            );
        } else {
            outboxEventPublisher.publish(
                    TRADE_SELL_ORDER_CREATE,
                    EventPayload.builder()
                            .takerId(matchOrder.getUserId())
                            .makerId(placeOrder.getUserId())
                            .takerTotalUsed(takerTotalUsed)
                            .makerTotalUsed(makerTotalUsed)
                            .matchedQuantity(matchedQty)
                            .symbol(matchOrder.getSymbol())
                            .build(),
                    trade.getId()
            );
        }

        if (placeOrder.isFullyFilled()) placeOrder.markCompleted();
    }

    public void refundUnmatchedLockedBalance(TradeOrder order, BigDecimal remainPrice) {
        if (remainPrice.compareTo(BigDecimal.ZERO) > 0) {
            outboxEventPublisher.publish(
                    REFUND_LOCKED_BALANCE,
                    EventPayload.builder()
                            .orderId(order.getId())
                            .userId(order.getUserId())
                            .totalRemainPrice(remainPrice)
                            .build(),
                    order.getUserId()
            );
        }
    }

    public BigDecimal calculateTradeFee(BigDecimal amount, TradeOrderRole role) {
        BigDecimal feeRate = (role == MAKER)
                ? feePolicy.getMakerFeeRate()
                : feePolicy.getTakerFeeRate();

        return amount.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }

    public Trade createAndSaveTrade(TradeOrder matchOrder, TradeOrder placeOrder, BigDecimal price, BigDecimal qty,
                                    TradeOrderSide takerSide, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime registeredDateTime) {
        return tradeRepository.save(Trade.create(
                matchOrder.getSymbol(), price, qty, takerSide.name(),
                matchOrder.getId(), placeOrder.getId(),
                matchOrder.getUserId(), placeOrder.getUserId(),
                takerFee, makerFee, registeredDateTime
        ));
    }
}
