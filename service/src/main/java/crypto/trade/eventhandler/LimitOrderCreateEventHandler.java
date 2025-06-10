package crypto.trade.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.payload.LimitOrderCreateEventPayload;
import crypto.trade.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class LimitOrderCreateEventHandler implements EventHandler<LimitOrderCreateEventPayload> {
    private final TradeRepository tradeRepository;

    @Override
    public void handle(Event<LimitOrderCreateEventPayload> event) {

    }

    @Override
    public boolean supports(Event<LimitOrderCreateEventPayload> event) {
        return EventType.LIMIT_ORDER_CREATE == event.getType();
    }
}
