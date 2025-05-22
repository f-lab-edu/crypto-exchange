package crypto.trade;

import crypto.order.Order;
import crypto.user.User;
import crypto.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Transactional
@RequiredArgsConstructor
@Service
public class TradeSettlementService {

    private final UserRepository userRepository;

    public void settle(BigDecimal amount, Trade trade, Order buyOrder, Order sellOrder) {
        BigDecimal takerFee = trade.getTakerFee();
        BigDecimal makerFee = trade.getMakerFee();

        User buyer = buyOrder.getUser();
        buyer.buyOrderSettlement(amount);
        buyer.feeSettlement(takerFee);

        User seller = sellOrder.getUser();
        seller.sellOrderSettlement(amount);
        seller.feeSettlement(makerFee);
    }
}
