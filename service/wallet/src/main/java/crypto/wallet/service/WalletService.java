package crypto.wallet.service;

import crypto.common.fee.FeePolicy;
import crypto.common.security.context.UserContext;
import crypto.wallet.controller.response.CheckBalanceResponse;
import crypto.wallet.controller.response.CheckQuantityResponse;
import crypto.wallet.entity.UserBalance;
import crypto.wallet.entity.UserCoin;
import crypto.wallet.service.exception.NotEnoughBalanceException;
import crypto.wallet.service.exception.NotEnoughQuantityException;
import crypto.wallet.service.request.CheckBalanceServiceRequest;
import crypto.wallet.service.request.CheckQuantityServiceRequest;


import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;


@RequiredArgsConstructor
public class WalletService {

    private final UserBalanceService userBalanceService;
    private final UserCoinService userCoinService;
    private final FeePolicy feePolicy;

    @Transactional
    public CheckBalanceResponse checkBalance(CheckBalanceServiceRequest request) {
        Long userId = UserContext.getUserId();
        UserBalance userBalance = userBalanceService.getUserBalanceOrThrow(userId);

        BigDecimal totalOrderPrice;

        if (request.getQuantity() != null) {
            totalOrderPrice = request.getPrice().multiply(request.getQuantity());
        } else {
            totalOrderPrice = request.getPrice();
        }

        BigDecimal orderFee = calculateOrderFee(totalOrderPrice);

        if (userBalance.getAvailableBalance().compareTo(totalOrderPrice.add(orderFee)) < 0) {
            throw new NotEnoughBalanceException();
        }

        userBalance.increaseLockedBalance(totalOrderPrice.add(orderFee));

        return CheckBalanceResponse.of(userId);
    }

    @Transactional
    public CheckQuantityResponse checkQuantity(CheckQuantityServiceRequest request) {
        Long userId = UserContext.getUserId();
        BigDecimal sellQuantity = request.getQuantity();
        UserCoin userCoin = userCoinService.getUserCoinOrThrow(userId, request.getSymbol());

        if (userCoin.getAvailableQuantity().compareTo(sellQuantity) < 0) {
            throw new NotEnoughQuantityException();
        }

        userCoin.increaseLockedQuantity(sellQuantity);

        return CheckQuantityResponse.of(userId);
    }

    private BigDecimal calculateOrderFee(BigDecimal totalPrice) {
        BigDecimal feeRate = feePolicy.getTakerFeeRate();

        return totalPrice.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }
}
