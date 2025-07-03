package crypto.wallet.repository;

import crypto.wallet.entity.UserCoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    Optional<UserCoin> findByUserAndCoinSymbol(Long userId, String symbol);
}
