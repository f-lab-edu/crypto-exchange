package crypto.settlementdata.repository;

import crypto.settlementdata.entity.UserCoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    Optional<UserCoin> findByUserIdAndSymbol(Long userId, String symbol);
}
