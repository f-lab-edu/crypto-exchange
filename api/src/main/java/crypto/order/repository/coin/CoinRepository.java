package crypto.order.repository.coin;

import crypto.order.entity.coin.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CoinRepository extends JpaRepository<Coin, Long> {

    Optional<Coin> findBySymbol(String symbol);
}
