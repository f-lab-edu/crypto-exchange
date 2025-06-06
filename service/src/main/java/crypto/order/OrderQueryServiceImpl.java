package crypto.order;

import crypto.coin.Coin;
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
}
