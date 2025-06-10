package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.MarketSellOrderCreateEventPayload;
import crypto.trade.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MarketSellOrderCreateEventHandler implements EventHandler<MarketSellOrderCreateEventPayload> {
    private final TradeRepository tradeRepository;

    @Override
    public void handle(Event<MarketSellOrderCreateEventPayload> event) {

    }

    @Override
    public boolean supports(Event<MarketSellOrderCreateEventPayload> event) {
        return EventType.MARKET_SELL_ORDER_CREATE == event.getType();
    }
}
