package crypto.order;

import crypto.context.UserContext;
import crypto.order.exception.OrderNotFoundException;
import crypto.order.request.LimitOrderServiceRequest;
import crypto.order.request.MarketBuyOrderServiceRequest;
import crypto.order.request.MarketSellOrderServiceRequest;
import crypto.order.response.*;
import crypto.time.TimeProvider;
import crypto.user.User;
import crypto.user.UserRepository;

import crypto.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderSide.*;
import static crypto.order.OrderStatus.*;


@Transactional
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TimeProvider timeProvider;

    public OrderCreateResponse createLimitBuyOrder(LimitOrderServiceRequest request) {
        LocalDateTime registeredDateTime = timeProvider.now();
        Order order = orderRepository.save(setLimitOrder(request, BUY, getUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderCreateResponse createLimitSellOrder(LimitOrderServiceRequest request) {
        LocalDateTime registeredDateTime = timeProvider.now();
        Order order = orderRepository.save(setLimitOrder(request, SELL, getUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderCreateResponse createMarketBuyOrder(MarketBuyOrderServiceRequest request) {
        String symbol = request.getSymbol();
        BigDecimal totalPrice = request.getTotalPrice();
        LocalDateTime registeredDateTime = timeProvider.now();

        Order order = orderRepository.save(Order.createMarketBuyOrder(symbol, totalPrice, getUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderCreateResponse createMarketSellOrder(MarketSellOrderServiceRequest request) {
        String symbol = request.getSymbol();
        BigDecimal totalAmount = request.getTotalAmount();
        LocalDateTime registeredDateTime = timeProvider.now();

        Order order = orderRepository.save(Order.createMarketSellOrder(symbol, totalAmount, getUser(), registeredDateTime));

        return OrderCreateResponse.of(order);
    }

    public OrderDeleteResponse deleteOrder(Long orderId) {
        LocalDateTime deletedDateTime = timeProvider.now();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.setDeleted(deletedDateTime);

        return OrderDeleteResponse.of(order);
    }

    public OrderAvailableResponse getAvailableAmount() {
        User user = getUser();

        return OrderAvailableResponse.of(user);
    }

    public Page<CompleteOrderListResponse> getCompleteOrders(Pageable pageable) {

        return orderRepository.findByUserIdAndOrderStatus(getUser().getId(), FILLED, pageable)
                .map(CompleteOrderListResponse::of);
    }

    public Page<OpenOrderListResponse> getOpenOrders(Pageable pageable) {

        return orderRepository.findByUserIdAndOrderStatusIn(getUser().getId(), List.of(PARTIAL, OPEN), pageable)
                .map(OpenOrderListResponse::of);
    }

    private User getUser() {
        return userRepository.findById(UserContext.getUserId())
                .orElseThrow(UserNotFoundException::new);
    }

    private Order setLimitOrder(LimitOrderServiceRequest request, OrderSide orderSide, User user, LocalDateTime registeredDateTime) {
        String symbol = request.getSymbol();
        BigDecimal price = request.getPrice();
        BigDecimal quantity = request.getQuantity();

        return Order.createLimitOrder(symbol, price, quantity, orderSide, user, registeredDateTime);
    }
}
