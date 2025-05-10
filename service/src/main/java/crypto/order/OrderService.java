package crypto.order;

import crypto.context.UserContext;
import crypto.order.request.LimitOrderServiceRequest;
import crypto.order.request.MarketBuyOrderServiceRequest;
import crypto.order.request.MarketSellOrderServiceRequest;
import crypto.user.User;
import crypto.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static crypto.CommonResponseCode.*;
import static crypto.order.OrderSide.*;


@Transactional
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public Order createLimitBuyOrder(LimitOrderServiceRequest request, LocalDateTime registeredDateTime) {

        return orderRepository.save(setLimitOrder(request, BUY, getUser(), registeredDateTime));
    }

    public Order createLimitSellOrder(LimitOrderServiceRequest request, LocalDateTime registeredDateTime) {

        return orderRepository.save(setLimitOrder(request, SELL, getUser(), registeredDateTime));
    }

    public Order createMarketBuyOrder(MarketBuyOrderServiceRequest request, LocalDateTime registeredDateTime) {
        String symbol = request.getSymbol();
        BigDecimal totalPrice = request.getTotalPrice();

        Order order = Order.createMarketBuyOrder(symbol, totalPrice, getUser(), registeredDateTime);

        return orderRepository.save(order);
    }

    public Order createMarketSellOrder(MarketSellOrderServiceRequest request, LocalDateTime registeredDateTime) {
        String symbol = request.getSymbol();
        BigDecimal totalAmount = request.getTotalAmount();

        Order order = Order.createMarketSellOrder(symbol, totalAmount, getUser(), registeredDateTime);

        return orderRepository.save(order);
    }

    public Order deleteOrder(Long orderId, LocalDateTime deletedDateTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ORDER_NOT_FOUND.getMessage())
                );
        order.setDeleted(deletedDateTime);

        return order;
    }

    public User getUser() {
        return userRepository.findById(UserContext.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        USER_NOT_FOUND.getMessage())
                );
    }

    private Order setLimitOrder(LimitOrderServiceRequest request, OrderSide orderSide, User user, LocalDateTime registeredDateTime) {
        String symbol = request.getSymbol();
        BigDecimal price = request.getPrice();
        BigDecimal quantity = request.getQuantity();

        return Order.createLimitOrder(symbol, price, quantity, orderSide, user, registeredDateTime);
    }
}
