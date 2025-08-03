package crypto.settlement.repository;

import crypto.settlement.entity.UserCoin;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;


public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserCoin> findByUserIdAndSymbol(Long userId, String symbol);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserCoin uc SET uc.lockedQuantity = uc.lockedQuantity - :quantity " +
            "WHERE uc.userId = :userId AND uc.symbol = :symbol AND uc.lockedQuantity >= :quantity")
    int decreaseLockQuantity(@Param("userId") Long userId,
                             @Param("symbol") String symbol,
                             @Param("quantity") BigDecimal quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserCoin uc SET uc.availableQuantity = uc.availableQuantity + :quantity " +
            "WHERE uc.userId = :userId AND uc.symbol = :symbol")
    int increaseAvailableQuantity(@Param("userId") Long userId,
                                  @Param("symbol") String symbol,
                                  @Param("quantity") BigDecimal quantity);
}
