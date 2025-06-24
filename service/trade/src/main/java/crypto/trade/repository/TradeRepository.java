package crypto.trade.repository;

import crypto.trade.entity.Trade;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TradeRepository extends JpaRepository<Trade, Long> {
}
