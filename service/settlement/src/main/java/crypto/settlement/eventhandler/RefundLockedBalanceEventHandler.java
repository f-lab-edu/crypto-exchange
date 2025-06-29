package crypto.settlement.eventhandler;

import crypto.event.Event;

import crypto.event.EventType;
import crypto.event.payload.EventPayload;
import crypto.settlement.service.UserBalanceService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RefundLockedBalanceEventHandler implements EventHandler {

    private final UserBalanceService userBalanceService;

    @Override
    public void handle(Event event) {
        EventPayload payload = event.getPayload();

        userBalanceService.getUserBalanceOrThrow(payload.getUserId()).decreaseLockedBalance(payload.getTotalRemainPrice());
    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.REFUND_LOCKED_BALANCE;
    }
}
