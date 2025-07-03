package crypto.wallet.repository;

import crypto.wallet.entity.UserBalance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    Optional<UserBalance> findByUserId(Long userId);
}
