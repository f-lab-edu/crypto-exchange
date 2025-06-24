package crypto.order.service.user;

import crypto.order.entity.coin.Coin;
import crypto.order.entity.user.User;
import crypto.order.entity.user.UserCoin;
import crypto.order.repository.user.UserCoinRepository;
import crypto.order.service.user.exception.UserCoinNotFoundException;

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
