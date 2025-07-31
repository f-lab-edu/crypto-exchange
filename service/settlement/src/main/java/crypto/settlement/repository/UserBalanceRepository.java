package crypto.settlement.repository;

import crypto.settlement.entity.UserBalance;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserBalance> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserBalance ub SET ub.lockedBalance = ub.lockedBalance - :amount " +
            "WHERE ub.userId = :userId AND ub.lockedBalance >= :amount")
    int decreaseBalance(@Param("userId") Long userId, @Param("amount") Long amount);
}
