package crypto.settlement.eventhandler;

import crypto.event.Event;
import crypto.event.EventType;
import crypto.event.eventsender.OrderEventSender;
import crypto.event.payload.EventPayload;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import static crypto.event.EventType.ORDER_COMPLETE_EVENT;


@Component
@RequiredArgsConstructor
public class SellOrderSettlementEventHandler implements EventHandler {

    private final SettlementProcessor settlementProcessor;
    private final OrderEventSender orderEventSender;

    @Override
    public void handle(Event event) {
        EventPayload payload = event.getPayload();

        settlementProcessor.settleUser(payload.getTakerTotalUsed(), payload.getMakerTotalUsed(), payload.getMatchedQuantity(),
                payload.getTakerId(), payload.getMakerId(), payload.getSymbol(), "SELL");

        orderEventSender.sendFailCompleteEvent(
                ORDER_COMPLETE_EVENT,
                payload.getOrderId(),
                EventPayload.builder()
                        .orderId(payload.getOrderId())
                        .build()
        );
    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.SELL_ORDER_SETTLEMENT;
    }
}


