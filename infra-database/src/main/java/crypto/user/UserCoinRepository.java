package crypto.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    Optional<UserCoin> findByUserAndCoinSymbol(User user, String symbol);
}
