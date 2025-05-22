package crypto.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByUserIdAndOrderStatusIn(Long userId, List<OrderStatus> orderStatuses, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.symbol = :symbol
          AND o.orderSide = :orderSide
          AND o.orderStatus IN ('OPEN', 'PARTIAL')
          AND o.price <= :buyPrice
        ORDER BY o.price ASC, o.registeredDateTime ASC
    """)
    List<Order> findMatchedOrders(@Param("symbol") String symbol, @Param("orderSide") OrderSide orderSide, @Param("buyPrice") BigDecimal buyPrice);
}
