package crypto.trade;

import crypto.fee.FeePolicy;
import crypto.order.Order;
import crypto.order.OrderRepository;
import crypto.order.OrderRole;
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

    public void match(Order buyOrder) {
        LocalDateTime registeredDateTime = timeProvider.now();
        List<Order> sellOrders = orderRepository.findMatchedOrders(buyOrder.getSymbol(), SELL, buyOrder.getPrice());

        if (sellOrders.isEmpty()) {
            return;
        }

        for (Order sellOrder : sellOrders) {
            BigDecimal buyQuantity = buyOrder.getQuantity();
            BigDecimal sellQuantity = sellOrder.calculateRemainQuantity();

            BigDecimal matchedQty = buyQuantity.min(sellQuantity);
            BigDecimal matchedPrice = sellOrder.getPrice();
            BigDecimal amount = matchedPrice.multiply(matchedQty);

            BigDecimal takerFee = calculateTradeFee(amount, TAKER);
            BigDecimal makerFee = calculateTradeFee(amount, MAKER);

            buyOrder.fill(matchedQty);
            sellOrder.fill(matchedQty);

            Trade trade = tradeRepository.save(Trade.create(
                    buyOrder.getSymbol(), matchedPrice, matchedQty, BUY, buyOrder.getId(), sellOrder.getId(),
                    buyOrder.getUser().getId(), sellOrder.getUser().getId(), takerFee, makerFee,
                    registeredDateTime
            ));

            tradeSettlementService.settle(amount, trade, buyOrder, sellOrder);

            if (sellOrder.isFullyFilled()) {
                sellOrder.markCompleted();
            }

            if (buyOrder.isFullyFilled()) {
                buyOrder.markCompleted();
                break;
            }
        }
    }

    private BigDecimal calculateTradeFee(BigDecimal amount, OrderRole role) {
        BigDecimal feeRate = (role == MAKER)
                ? feePolicy.getMakerFeeRate()
                : feePolicy.getTakerFeeRate();

        return amount.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }
}
