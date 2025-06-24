package crypto.trade.repository;

import crypto.trade.entity.ProcessedEvent;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
}
