package crypto.settlement.eventhandler;

import crypto.settlement.service.UserBalanceService;
import crypto.settlement.service.UserCoinService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
public class SettlementProcessor {

    private final UserBalanceService userBalanceService;
    private final UserCoinService userCoinService;

    public void settleUser(BigDecimal takerTotalPrice, BigDecimal makerTotalPrice, BigDecimal matchedQuantity,
                           Long takerId, Long makerId, String symbol, String orderSide) {
        if (orderSide.equals("BUY")) {
            userBalanceService.getUserBalanceOrThrow(takerId).decreaseLockedBalance(takerTotalPrice);
            userBalanceService.getUserBalanceOrThrow(makerId).increaseAvailableBalance(makerTotalPrice);

            userCoinService.getUserCoinOrCreate(takerId, symbol).increaseQuantity(matchedQuantity);
            userCoinService.getUserCoinOrThrow(makerId, symbol).decreaseLockQuantity(matchedQuantity);
        } else {
            userBalanceService.getUserBalanceOrThrow(makerId).increaseAvailableBalance(makerTotalPrice);
            userBalanceService.getUserBalanceOrThrow(takerId).decreaseLockedBalance(takerTotalPrice);

            userCoinService.getUserCoinOrThrow(makerId, symbol).decreaseLockQuantity(matchedQuantity);
            userCoinService.getUserCoinOrCreate(takerId, symbol).increaseQuantity(matchedQuantity);
        }
    }
}
