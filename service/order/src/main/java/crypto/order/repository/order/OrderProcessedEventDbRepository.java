package crypto.order.repository.order;

import crypto.order.entity.order.OrderProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProcessedEventDbRepository extends JpaRepository<OrderProcessedEvent, String> {
}
