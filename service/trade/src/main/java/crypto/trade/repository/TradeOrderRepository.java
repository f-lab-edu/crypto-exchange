package crypto.trade.repository;

import crypto.trade.entity.TradeOrder;
import crypto.trade.entity.TradeOrderSide;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    @Query("""
        SELECT o FROM TradeOrder o
        WHERE o.symbol = :symbol
          AND o.orderSide = :orderSide
          AND o.orderStatus = :#{T(crypto.trade.entity.TradeOrderStatus).OPEN}
          AND o.price <= :buyPrice
        ORDER BY o.price ASC, o.registeredDateTime ASC
    """)
    List<TradeOrder> findMatchedLimitBuyOrders(@Param("symbol") String symbol, @Param("orderSide") TradeOrderSide orderSide, @Param("buyPrice") BigDecimal buyPrice);

    @Query("""
        SELECT o FROM TradeOrder o
        WHERE o.symbol = :symbol
          AND o.orderSide = :orderSide
          AND o.orderStatus = :#{T(crypto.trade.entity.TradeOrderStatus).OPEN}
          AND o.price >= :sellPrice
        ORDER BY o.price DESC, o.registeredDateTime ASC
    """)
    List<TradeOrder> findMatchedLimitSellOrders(@Param("symbol") String symbol, @Param("orderSide") TradeOrderSide orderSide, @Param("sellPrice") BigDecimal sellPrice);

    @Query("""
        SELECT o FROM TradeOrder o
        WHERE o.symbol = :symbol
          AND o.orderSide = :orderSide
          AND o.orderStatus = :#{T(crypto.trade.entity.TradeOrderStatus).OPEN}
        ORDER BY o.price ASC, o.registeredDateTime ASC
    """)
    List<TradeOrder> findMatchedMarketBuyOrders(@Param("symbol") String symbol, @Param("orderSide") TradeOrderSide orderSide);

    @Query("""
        SELECT o FROM TradeOrder o
        WHERE o.symbol = :symbol
          AND o.orderSide = :orderSide
          AND o.orderStatus = :#{T(crypto.trade.entity.TradeOrderStatus).OPEN}
        ORDER BY o.price DESC, o.registeredDateTime ASC
    """)
    List<TradeOrder> findMatchedMarketSellOrders(@Param("symbol") String symbol, @Param("orderSide") TradeOrderSide orderSide);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TradeOrder o " +
            "SET o.filledQuantity = o.filledQuantity + :quantityToFill " +
            "WHERE o.id = :orderId AND (o.quantity - o.filledQuantity) >= :quantityToFill")
    int fillAtomically(@Param("orderId") Long orderId, @Param("quantityToFill") BigDecimal quantityToFill);

}
