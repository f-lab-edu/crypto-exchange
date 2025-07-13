package crypto.settlement.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.EventPayload;
import crypto.settlementdata.entity.UserCoin;
import crypto.settlementdata.service.UserCoinService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BuyOrderSettlementEventHandler implements EventHandler {

    private final SettlementProcessor settlementProcessor;
    private final UserCoinService userCoinService;

    @Override
    public void handle(Event event) {
        EventPayload payload = event.getPayload();

        settlementProcessor.settleUser(payload.getTakerTotalUsed(), payload.getMakerTotalUsed(),
                payload.getTakerId(), payload.getMakerId(), "BUY");

        UserCoin buyerCoin = userCoinService.getUserCoinOrCreate(payload.getTakerId(), payload.getSymbol());
        buyerCoin.increaseQuantity(payload.getMatchedQuantity());

        UserCoin sellerCoin = userCoinService.getUserCoinOrThrow(payload.getMakerId(), payload.getSymbol());
        sellerCoin.decreaseLockQuantity(payload.getMatchedQuantity());
    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.BUY_ORDER_SETTLEMENT;
    }
}
