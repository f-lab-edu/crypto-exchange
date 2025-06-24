package crypto.order.service.order;

import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import java.math.BigDecimal;
import java.util.List;


public interface OrderQueryService {
    List<Order> getMatchedLimitBuyOrders(Coin coin, OrderSide orderSide, BigDecimal buyPrice);
    List<Order> getMatchedLimitSellOrders(Coin coin, OrderSide orderSide, BigDecimal sellPrice);
    List<Order> getMatchedMarketBuyOrders(Coin coin, OrderSide orderSide);
    List<Order> getMatchedMarketSellOrders(Coin coin, OrderSide orderSide);
    Order findOrder(Long orderId);
}

