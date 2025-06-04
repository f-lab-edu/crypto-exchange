package crypto.trade;

import crypto.order.Order;
import crypto.order.OrderSide;
import crypto.user.User;

import crypto.user.UserCoin;
import crypto.user.UserCoinService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static crypto.order.OrderSide.*;


@RequiredArgsConstructor
@Service
public class TradeSettlementService {

    private final UserCoinService userCoinService;

    public void buyOrderSettle(BigDecimal takerTotalPrice, BigDecimal makerTotalPrice, Trade trade, Order buyOrder, Order sellOrder) {
        User buyer = buyOrder.getUser();
        User seller = sellOrder.getUser();

        settleUser(takerTotalPrice, makerTotalPrice, buyer, seller, BUY);

        UserCoin buyerCoin = userCoinService.getUserCoinOrCreate(buyer, trade.getSymbol(), buyOrder.getCoin());
        buyerCoin.increaseQuantity(trade.getQuantity());

        UserCoin sellerCoin = userCoinService.getUserCoinOrThrow(seller, trade.getSymbol());
        sellerCoin.decreaseLockQuantity(trade.getQuantity());
    }

    public void sellOrderSettle(BigDecimal takerTotalPrice, BigDecimal makerTotalPrice, Trade trade, Order sellOrder, Order buyOrder) {
        User seller = sellOrder.getUser();
        User buyer = buyOrder.getUser();

        settleUser(takerTotalPrice, makerTotalPrice, seller, buyer, SELL);

        UserCoin sellerCoin = userCoinService.getUserCoinOrThrow(seller, trade.getSymbol());
        sellerCoin.decreaseLockQuantity(trade.getQuantity());

        UserCoin buyerCoin = userCoinService.getUserCoinOrThrow(buyer, trade.getSymbol());
        buyerCoin.increaseQuantity(trade.getQuantity());
    }

    public void refundUnmatchedLockedBalance(Order order, BigDecimal remainPrice) {
        if (remainPrice.compareTo(BigDecimal.ZERO) > 0) {
            order.getUser().getUserBalance().decreaseLockedBalance(remainPrice);
        }
    }

    private void settleUser(BigDecimal takerTotalPrice, BigDecimal makerTotalPrice, User taker, User maker, OrderSide orderSide) {
        if (orderSide.equals(BUY)) {
            taker.getUserBalance().decreaseLockedBalance(takerTotalPrice);
            maker.getUserBalance().increaseAvailableBalance(makerTotalPrice);
        } else {
            taker.getUserBalance().increaseAvailableBalance(takerTotalPrice);
            maker.getUserBalance().decreaseLockedBalance(makerTotalPrice);
        }
    }
}
