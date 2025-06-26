package crypto.settlement.repository;

import crypto.settlement.entity.SettlementProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SettlementProcessedEventDbRepository extends JpaRepository<SettlementProcessedEvent, String> {
}
