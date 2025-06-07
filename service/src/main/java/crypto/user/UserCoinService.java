package crypto.user;

import crypto.coin.Coin;
import crypto.user.exception.UserCoinNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@RequiredArgsConstructor
@Service
public class UserCoinService {

    private final UserCoinRepository userCoinRepository;

    public UserCoin getUserCoinOrThrow(User user, String symbol) {
        return userCoinRepository.findByUserAndCoinSymbol(user, symbol)
                .orElseThrow(UserCoinNotFoundException::new);
    }

    public UserCoin getUserCoinOrCreate(User user, String symbol, Coin coin) {
        return userCoinRepository.findByUserAndCoinSymbol(user, symbol)
                .orElseGet(() -> userCoinRepository.save(UserCoin.create(user, coin)));
    }
}
