package crypto.trade;

import crypto.fee.FeePolicy;
import crypto.order.Order;
import crypto.order.OrderRepository;
import crypto.order.OrderRole;
import crypto.order.OrderSide;
import crypto.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderRole.MAKER;
import static crypto.order.OrderRole.TAKER;
import static crypto.order.OrderSide.BUY;
import static crypto.order.OrderSide.SELL;


@Transactional
@RequiredArgsConstructor
@Service
public class TradeService {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TradeSettlementService tradeSettlementService;
    private final TimeProvider timeProvider;
    private final FeePolicy feePolicy;

    public void limitBuyOrderMatch(Order buyOrder) {
        LocalDateTime registeredDateTime = timeProvider.now();
        List<Order> sellOrders = orderRepository.findMatchedLimitBuyOrders(buyOrder.getCoin(), SELL, buyOrder.getPrice());

        if (sellOrders.isEmpty()) {
            return;
        }

        for (Order sellOrder : sellOrders) {
            processMatchLimitOrder(buyOrder, sellOrder, BUY, registeredDateTime);

            if (sellOrder.isFullyFilled()) {
                sellOrder.markCompleted();
            }

            if (buyOrder.isFullyFilled()) {
                buyOrder.markCompleted();
                break;
            }
        }
    }

    public void limitSellOrderMatch(Order sellOrder) {
        LocalDateTime registeredDateTime = timeProvider.now();
        List<Order> buyOrders = orderRepository.findMatchedLimitSellOrders(sellOrder.getCoin(), BUY, sellOrder.getPrice());

        if (buyOrders.isEmpty()) {
            return;
        }

        for (Order buyOrder : buyOrders) {
            processMatchLimitOrder(sellOrder, buyOrder, SELL, registeredDateTime);

            if (buyOrder.isFullyFilled()) {
                buyOrder.markCompleted();
            }

            if (sellOrder.isFullyFilled()) {
                sellOrder.markCompleted();
                break;
            }
        }
    }

    public void marketBuyOrderMatch(Order buyOrder) {
        LocalDateTime registeredDateTime = timeProvider.now();
        List<Order> sellOrders = orderRepository.findMatchedMarketBuyOrders(buyOrder.getCoin(), SELL);

        BigDecimal remainPrice = buyOrder.getTotalPrice();

        for (Order sellOrder : sellOrders) {
            BigDecimal sellPrice = sellOrder.getPrice();
            BigDecimal sellRemainQty = sellOrder.calculateRemainQuantity();
            BigDecimal maxBuyQty = remainPrice.divide(sellPrice, 8, RoundingMode.DOWN);
            BigDecimal matchedQty = maxBuyQty.min(sellRemainQty);

            if (matchedQty.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal matchedAmount = sellPrice.multiply(matchedQty);
            BigDecimal takerFee = calculateTradeFee(matchedAmount, TAKER);
            BigDecimal makerFee = calculateTradeFee(matchedAmount, MAKER);
            BigDecimal takerTotalUsed = matchedAmount.add(takerFee);
            BigDecimal makerTotalUsed = matchedAmount.add(makerFee);

            if (takerTotalUsed.compareTo(remainPrice) > 0) break;

            Trade trade = createAndSaveTrade(buyOrder, sellOrder, sellPrice, matchedQty, BUY, takerFee, makerFee, registeredDateTime);
            settleAndMarkOrders(buyOrder, sellOrder, matchedQty, takerTotalUsed, makerTotalUsed, trade, BUY);
            remainPrice = remainPrice.subtract(takerTotalUsed);

            if (buyOrder.isFullyFilled()) break;

        }
    }

    public void marketSellOrderMatch(Order sellOrder) {
        LocalDateTime registeredDateTime = timeProvider.now();
        List<Order> buyOrders = orderRepository.findMatchedMarketSellOrders(sellOrder.getCoin(), BUY);

        BigDecimal remainQty = sellOrder.getTotalAmount();

        for (Order buyOrder : buyOrders) {
            BigDecimal buyPrice = buyOrder.getPrice();
            BigDecimal buyRemainQty = buyOrder.calculateRemainQuantity();
            BigDecimal matchedQty = remainQty.min(buyRemainQty);

            if (matchedQty.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal matchedAmount = buyPrice.multiply(matchedQty);
            BigDecimal takerFee = calculateTradeFee(matchedAmount, TAKER);
            BigDecimal makerFee = calculateTradeFee(matchedAmount, MAKER);
            BigDecimal takerTotalUsed = matchedAmount.add(takerFee);
            BigDecimal makerTotalUsed = matchedAmount.add(makerFee);

            Trade trade = createAndSaveTrade(sellOrder, buyOrder, buyPrice, matchedQty, SELL, takerFee, makerFee, registeredDateTime);
            remainQty = remainQty.subtract(matchedQty);
            settleAndMarkOrders(sellOrder, buyOrder, matchedQty, takerTotalUsed, makerTotalUsed, trade, SELL);

            if (sellOrder.isFullyFilled()) break;
        }
    }

    private void processMatchLimitOrder(Order matchOrder, Order placeOrder, OrderSide orderSide, LocalDateTime registeredDateTime) {
        BigDecimal matchOrderQuantity = matchOrder.getQuantity();
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

    private Trade createAndSaveTrade(Order matchOrder, Order placeOrder, BigDecimal price, BigDecimal qty,
                                     OrderSide takerSide, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime registeredDateTime) {
        return tradeRepository.save(Trade.create(
                matchOrder.getCoin().getSymbol(), price, qty, takerSide,
                matchOrder.getId(), placeOrder.getId(),
                matchOrder.getUser().getId(), placeOrder.getUser().getId(),
                takerFee, makerFee, registeredDateTime
        ));
    }

    private void settleAndMarkOrders(Order matchOrder, Order placeOrder, BigDecimal matchedQty, BigDecimal takerTotalUsed,
                                     BigDecimal makerTotalUsed, Trade trade, OrderSide orderSide) {
        matchOrder.fill(matchedQty);
        placeOrder.fill(matchedQty);

        if (orderSide.equals(BUY)) {
            tradeSettlementService.buyOrderSettle(takerTotalUsed, makerTotalUsed, trade, matchOrder, placeOrder);
        } else {
            tradeSettlementService.sellOrderSettle(takerTotalUsed, makerTotalUsed, trade, matchOrder, placeOrder);
        }

        if (matchOrder.isFullyFilled()) matchOrder.markCompleted();
        if (placeOrder.isFullyFilled()) placeOrder.markCompleted();
    }

    private BigDecimal calculateTradeFee(BigDecimal amount, OrderRole role) {
        BigDecimal feeRate = (role == MAKER)
                ? feePolicy.getMakerFeeRate()
                : feePolicy.getTakerFeeRate();

        return amount.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }
}
