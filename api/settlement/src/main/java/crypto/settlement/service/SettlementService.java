package crypto.settlement.service;

import crypto.common.fee.FeePolicy;
import crypto.common.security.context.UserContext;
import crypto.settlement.controller.response.CheckBalanceResponse;
import crypto.settlement.controller.response.CheckQuantityResponse;
import crypto.settlement.service.exception.NotEnoughBalanceException;
import crypto.settlement.service.exception.NotEnoughQuantityException;
import crypto.settlement.service.request.CheckBalanceServiceRequest;
import crypto.settlement.service.request.CheckQuantityServiceRequest;
import crypto.settlementdata.entity.UserBalance;
import crypto.settlementdata.entity.UserCoin;
import crypto.settlementdata.service.UserBalanceService;
import crypto.settlementdata.service.UserCoinService;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;


@RequiredArgsConstructor
public class SettlementService {

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
