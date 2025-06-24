package crypto.order.repository.order;

import crypto.order.entity.coin.Coin;
import crypto.order.entity.order.Order;
import crypto.order.entity.order.OrderSide;
import crypto.order.entity.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.coin = :coin
          AND o.orderSide = :orderSide
          AND o.orderStatus = 'OPEN'
          AND o.orderType = 'LIMIT'
          AND o.price <= :buyPrice
        ORDER BY o.price ASC, o.registeredDateTime ASC
    """)
    List<Order> findMatchedLimitBuyOrders(@Param("coin") Coin coin, @Param("orderSide") OrderSide orderSide, @Param("buyPrice") BigDecimal buyPrice);

    @Query("""
        SELECT o FROM Order o
        WHERE o.coin = :coin
          AND o.orderSide = :orderSide
          AND o.orderStatus = 'OPEN'
          AND o.orderType = 'LIMIT'
          AND o.price >= :sellPrice
        ORDER BY o.price DESC, o.registeredDateTime ASC
    """)
    List<Order> findMatchedLimitSellOrders(@Param("coin") Coin coin, @Param("orderSide") OrderSide orderSide, @Param("sellPrice") BigDecimal sellPrice);

    @Query("""
        SELECT o FROM Order o
        WHERE o.coin = :coin
          AND o.orderSide = :orderSide
          AND o.orderStatus = 'OPEN'
          AND o.orderType = 'LIMIT'
        ORDER BY o.price ASC, o.registeredDateTime ASC
    """)
    List<Order> findMatchedMarketBuyOrders(@Param("coin") Coin coin, @Param("orderSide") OrderSide orderSide);

    @Query("""
        SELECT o FROM Order o
        WHERE o.coin = :coin
          AND o.orderSide = :orderSide
          AND o.orderStatus = 'OPEN'
          AND o.orderType = 'LIMIT'
        ORDER BY o.price DESC, o.registeredDateTime ASC
    """)
    List<Order> findMatchedMarketSellOrders(@Param("coin") Coin coin, @Param("orderSide") OrderSide orderSide);
}
