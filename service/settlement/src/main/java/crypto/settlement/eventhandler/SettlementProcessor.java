package crypto.settlement.eventhandler;

import crypto.settlement.service.UserBalanceService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
public class SettlementProcessor {

    private final UserBalanceService userBalanceService;

    public void settleUser(BigDecimal takerTotalPrice, BigDecimal makerTotalPrice, Long takerId, Long makerId, String orderSide) {
        if (orderSide.equals("BUY")) {
            userBalanceService.getUserBalanceOrThrow(takerId).decreaseLockedBalance(takerTotalPrice);
            userBalanceService.getUserBalanceOrThrow(makerId).increaseAvailableBalance(makerTotalPrice);
        } else {
            userBalanceService.getUserBalanceOrThrow(makerId).increaseAvailableBalance(makerTotalPrice);
            userBalanceService.getUserBalanceOrThrow(takerId).decreaseLockedBalance(takerTotalPrice);
        }
    }
}
