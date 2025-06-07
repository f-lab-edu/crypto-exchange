package crypto.order;

import crypto.coin.Coin;

import java.math.BigDecimal;
import java.util.List;


public interface OrderQueryService {
    List<Order> getMatchedLimitBuyOrders(Coin coin, OrderSide orderSide, BigDecimal buyPrice);
    List<Order> getMatchedLimitSellOrders(Coin coin, OrderSide orderSide, BigDecimal sellPrice);
    List<Order> getMatchedMarketBuyOrders(Coin coin, OrderSide orderSide);
    List<Order> getMatchedMarketSellOrders(Coin coin, OrderSide orderSide);
}

