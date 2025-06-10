package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.MarketBuyOrderCreateEventPayload;
import crypto.trade.TradeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MarketBuyOrderCreateEventHandler implements EventHandler<MarketBuyOrderCreateEventPayload> {
    private final TradeRepository tradeRepository;

    @Override
    public void handle(Event<MarketBuyOrderCreateEventPayload> event) {

    }

    @Override
    public boolean supports(Event<MarketBuyOrderCreateEventPayload> event) {
        return EventType.MARKET_BUY_ORDER_CREATE == event.getType();
    }
}
