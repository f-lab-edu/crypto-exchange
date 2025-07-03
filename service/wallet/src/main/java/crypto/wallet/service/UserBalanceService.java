package crypto.wallet.service;

import crypto.wallet.entity.UserBalance;
import crypto.wallet.repository.UserBalanceRepository;
import crypto.wallet.service.exception.UserBalanceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@RequiredArgsConstructor
@Service
public class UserBalanceService {

    private final UserBalanceRepository userBalanceRepository;

    public UserBalance getUserBalanceOrThrow(Long userId) {
        return userBalanceRepository.findByUserId(userId)
                .orElseThrow(UserBalanceNotFoundException::new);
    }
}
