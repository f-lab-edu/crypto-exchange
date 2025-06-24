package crypto.settlement.service;

import crypto.common.fee.FeePolicy;
import crypto.common.security.context.UserContext;
import crypto.event.Event;
import crypto.settlement.controller.response.CheckBalanceResponse;
import crypto.settlement.entity.UserBalance;
import crypto.settlement.eventhandler.EventHandler;
import crypto.settlement.repository.UserBalanceRepository;
import crypto.settlement.service.exception.NotEnoughBalanceException;
import crypto.settlement.service.exception.UserBalanceNotFoundException;
import crypto.settlement.service.request.CheckBalanceServiceRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@RequiredArgsConstructor
@Service
public class SettlementService {

    private final UserBalanceService userBalanceService;
    private final List<EventHandler> eventHandlers;
    private final FeePolicy feePolicy;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();

        EventHandler eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }
        eventHandler.handle(event);
    }

    @Transactional
    public CheckBalanceResponse checkBalance(CheckBalanceServiceRequest request) {
        Long userId = UserContext.getUserId();
        UserBalance userBalance = userBalanceService.getUserBalanceOrThrow(userId);

        BigDecimal totalOrderPrice;

        if (request.getQuantity() != null) {
            totalOrderPrice = request.getPrice().multiply(request.getQuantity());
        } else {
            totalOrderPrice = request.getPrice();
        }

        BigDecimal orderFee = calculateOrderFee(totalOrderPrice);

        if (userBalance.getAvailableBalance().compareTo(totalOrderPrice.add(orderFee)) < 0) {
            throw new NotEnoughBalanceException();
        }

        userBalance.increaseLockedBalance(totalOrderPrice.add(orderFee));

        return CheckBalanceResponse.of(userId);
    }

    private EventHandler findEventHandler(Event event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    private BigDecimal calculateOrderFee(BigDecimal totalPrice) {
        BigDecimal feeRate = feePolicy.getTakerFeeRate();

        return totalPrice.multiply(feeRate).setScale(8, RoundingMode.DOWN);
    }
}
