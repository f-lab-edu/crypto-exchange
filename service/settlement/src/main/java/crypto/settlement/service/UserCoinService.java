package crypto.settlement.service;

import crypto.settlement.entity.UserCoin;
import crypto.settlement.repository.UserCoinRepository;
import crypto.settlement.service.exception.UserCoinNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Transactional
@RequiredArgsConstructor
@Service
public class UserCoinService {

    private final UserCoinRepository userCoinRepository;

    public UserCoin getUserCoinOrThrow(Long userId, String symbol) {
        return userCoinRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(UserCoinNotFoundException::new);
    }

    public UserCoin getUserCoinOrCreate(Long userId, String symbol) {
        return userCoinRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseGet(() -> userCoinRepository.save(UserCoin.create(userId, symbol)));
    }

    public void decreaseLockQuantity(Long userId, String symbol, BigDecimal quantity) {
        int updatedRows = userCoinRepository.decreaseLockQuantity(userId, symbol, quantity);

        if (updatedRows == 0) {
            throw new UserCoinNotFoundException();
        }
    }

    public void increaseAvailableQuantity(Long userId, String symbol, BigDecimal quantity) {
        int updatedRows = userCoinRepository.increaseAvailableQuantity(userId, symbol, quantity);

        if (updatedRows == 0) {
            throw new UserCoinNotFoundException();
        }
    }
}
