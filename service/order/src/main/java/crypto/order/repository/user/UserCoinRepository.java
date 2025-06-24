package crypto.order.repository.user;

import crypto.order.entity.user.User;
import crypto.order.entity.user.UserCoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    Optional<UserCoin> findByUserAndCoinSymbol(User user, String symbol);
}
