package crypto.trade.repository;

import crypto.trade.entity.TradeProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TradeProcessedEventDbRepository extends JpaRepository<TradeProcessedEvent, String> {
}
