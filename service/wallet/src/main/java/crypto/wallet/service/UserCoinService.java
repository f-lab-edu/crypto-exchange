package crypto.wallet.service;

import crypto.wallet.entity.UserCoin;
import crypto.wallet.repository.UserCoinRepository;
import crypto.wallet.service.exception.UserCoinNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@RequiredArgsConstructor
@Service
public class UserCoinService {

    private final UserCoinRepository userCoinRepository;

    public UserCoin getUserCoinOrThrow(Long userId, String symbol) {
        return userCoinRepository.findByUserAndCoinSymbol(userId, symbol)
                .orElseThrow(UserCoinNotFoundException::new);
    }

    public UserCoin getUserCoinOrCreate(Long userId, String symbol) {
        return userCoinRepository.findByUserAndCoinSymbol(userId, symbol)
                .orElseGet(() -> userCoinRepository.save(UserCoin.create(userId, symbol)));
    }
}
