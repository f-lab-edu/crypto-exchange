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
            userBalanceService.decreaseLockBalance(takerId, takerTotalPrice);
            userBalanceService.increaseAvailableBalance(makerId, makerTotalPrice);

            userCoinService.increaseAvailableQuantity(takerId, symbol, matchedQuantity);
            userCoinService.decreaseLockQuantity(makerId, symbol, matchedQuantity);
        } else {
            userBalanceService.increaseAvailableBalance(makerId, makerTotalPrice);
            userBalanceService.decreaseLockBalance(takerId, takerTotalPrice);

            userCoinService.decreaseLockQuantity(makerId, symbol, matchedQuantity);
            userCoinService.increaseAvailableQuantity(takerId, symbol, matchedQuantity);
        }
    }
}
