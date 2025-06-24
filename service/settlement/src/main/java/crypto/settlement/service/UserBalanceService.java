package crypto.settlement.service;

import crypto.settlement.entity.UserBalance;
import crypto.settlement.repository.UserBalanceRepository;
import crypto.settlement.service.exception.UserBalanceNotFoundException;

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
