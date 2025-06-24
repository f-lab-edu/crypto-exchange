package crypto.order.service.order;

import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import crypto.order.repository.order.OrderRepository;
import crypto.order.service.order.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {
    private final OrderRepository orderRepository;

    public List<Order> getMatchedLimitBuyOrders(Coin coin, OrderSide orderSide, BigDecimal buyPrice) {
        return orderRepository.findMatchedLimitBuyOrders(coin, orderSide, buyPrice);
    }

    public List<Order> getMatchedLimitSellOrders(Coin coin, OrderSide orderSide, BigDecimal sellPrice) {
        return orderRepository.findMatchedLimitSellOrders(coin, orderSide, sellPrice);
    }

    public List<Order> getMatchedMarketBuyOrders(Coin coin, OrderSide orderSide) {
        return orderRepository.findMatchedMarketBuyOrders(coin, orderSide);
    }

    public List<Order> getMatchedMarketSellOrders(Coin coin, OrderSide orderSide) {
        return orderRepository.findMatchedMarketSellOrders(coin, orderSide);
    }

    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }
}
