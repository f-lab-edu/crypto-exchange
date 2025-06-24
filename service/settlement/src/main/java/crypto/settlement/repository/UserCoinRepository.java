package crypto.settlement.repository;

import crypto.settlement.entity.UserCoin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    Optional<UserCoin> findByUserAndCoinSymbol(Long userId, String symbol);
}
