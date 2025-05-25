package crypto.order;

import crypto.fee.FeePolicy;
import crypto.order.exception.NotEnoughBalanceException;
import crypto.order.exception.OrderNotFoundException;
import crypto.order.request.LimitOrderServiceRequest;
import crypto.order.request.MarketBuyOrderServiceRequest;
import crypto.order.request.MarketSellOrderServiceRequest;
import crypto.order.response.*;
import crypto.time.TimeProvider;
import crypto.trade.TradeService;
import crypto.user.User;

import crypto.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderSide.*;
import static crypto.order.OrderStatus.*;


@Transactional
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final TradeService tradeService;
    private final TimeProvider timeProvider;
    private final FeePolicy feePolicy;

    public OrderCreateResponse createLimitBuyOrder(LimitOrderServiceRequest request) {
        User user = userService.getCurrentUser();
        LocalDateTime registeredDateTime = timeProvider.now();
        BigDecimal totalOrderPrice = request.getPrice().multiply(request.getQuantity());
        BigDecimal orderFee = calculateOrderFee(totalOrderPrice);

        if (user.getAvailableBalance().compareTo(totalOrderPrice.add(orderFee)) < 0) {
            throw new NotEnoughBalanceException();
        }

        user.increaseLockedBalance(totalOrderPrice);
        Order order = orderRepository.save(buildLimitOrder(request, BUY, user, registeredDateTime));

        tradeService.match(order);

        return OrderCreateResponse.of(order);
    }

    public OrderCreateResponse createLimitSellOrder(LimitOrderServiceRequest request) {
        LocalDateTime registeredDateTime = timeProvider.now();
        Order order = orderRepository.save(buildLimitOrder(request, SELL, userService.getCurrentUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderCreateResponse createMarketBuyOrder(MarketBuyOrderServiceRequest request) {
        String symbol = request.getSymbol();
        BigDecimal totalPrice = request.getTotalPrice();
        LocalDateTime registeredDateTime = timeProvider.now();

        Order order = orderRepository.save(Order.createMarketBuyOrder(symbol, totalPrice, userService.getCurrentUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderCreateResponse createMarketSellOrder(MarketSellOrderServiceRequest request) {
        String symbol = request.getSymbol();
        BigDecimal totalAmount = request.getTotalAmount();
        LocalDateTime registeredDateTime = timeProvider.now();

        Order order = orderRepository.save(Order.createMarketSellOrder(symbol, totalAmount, userService.getCurrentUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderDeleteResponse deleteOrder(Long orderId) {
        LocalDateTime deletedDateTime = timeProvider.now();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.markDeleted(deletedDateTime);

        return OrderDeleteResponse.of(order);
    }

    public OrderAvailableResponse getAvailableAmount() {
        User user = userService.getCurrentUser();

        return OrderAvailableResponse.of(user);
    }

    public Page<CompleteOrderListResponse> getCompleteOrders(Pageable pageable) {

        return orderRepository.findByUserIdAndOrderStatus(userService.getCurrentUser().getId(), FILLED, pageable)
                .map(CompleteOrderListResponse::of);
    }

    public Page<OpenOrderListResponse> getOpenOrders(Pageable pageable) {

        return orderRepository.findByUserIdAndOrderStatusIn(userService.getCurrentUser().getId(), List.of(PARTIAL, OPEN), pageable)
                .map(OpenOrderListResponse::of);
    }

    private Order buildLimitOrder(LimitOrderServiceRequest request, OrderSide orderSide, User user, LocalDateTime registeredDateTime) {
        String symbol = request.getSymbol();
        BigDecimal price = request.getPrice();
        BigDecimal quantity = request.getQuantity();

        return Order.createLimitOrder(symbol, price, quantity, orderSide, user, registeredDateTime);
    }

    private BigDecimal calculateOrderFee(BigDecimal totalPrice) {
        BigDecimal feeRate = feePolicy.getTakerFeeRate();

        return totalPrice.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }
}
