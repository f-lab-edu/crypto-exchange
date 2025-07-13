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
public class SellOrderSettlementEventHandler implements EventHandler {

    private final SettlementProcessor settlementProcessor;
    private final UserCoinService userCoinService;

    @Override
    public void handle(Event event) {
        EventPayload payload = event.getPayload();

        settlementProcessor.settleUser(payload.getTakerTotalUsed(), payload.getMakerTotalUsed(),
                payload.getTakerId(), payload.getMakerId(), "SELL");

        UserCoin sellerCoin = userCoinService.getUserCoinOrThrow(payload.getTakerId(), payload.getSymbol());
        sellerCoin.decreaseLockQuantity(payload.getMatchedQuantity());

        UserCoin buyerCoin = userCoinService.getUserCoinOrCreate(payload.getMakerId(), payload.getSymbol());
        buyerCoin.increaseQuantity(payload.getMatchedQuantity());
    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.SELL_ORDER_SETTLEMENT;
    }
}


