package crypto.settlement.service;

import crypto.common.fee.FeePolicy;
import crypto.common.security.context.UserContext;
import crypto.event.Event;
import crypto.settlement.controller.response.CheckBalanceResponse;
import crypto.settlement.entity.SettlementProcessedEvent;
import crypto.settlement.entity.UserBalance;
import crypto.settlement.eventhandler.EventHandler;
import crypto.settlement.repository.SettlementProcessedEventDbRepository;
import crypto.settlement.repository.SettlementProcessedEventRepository;
import crypto.settlement.service.exception.NotEnoughBalanceException;
import crypto.settlement.service.request.CheckBalanceServiceRequest;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementService {

    private final UserBalanceService userBalanceService;
    private final List<EventHandler> eventHandlers;
    private final SettlementProcessedEventRepository settlementProcessedEventRepository;
    private final SettlementProcessedEventDbRepository settlementProcessedEventDbRepository;
    private final FeePolicy feePolicy;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getEventId();
        String key = settlementProcessedEventRepository.generateKey(eventId);

        Boolean isNewEvent = settlementProcessedEventRepository.setIfAbsent(eventId);

        if (Boolean.FALSE.equals(isNewEvent)) {
            log.info("[SettlementService.handleEvent] Already processed or is being processed. eventId={}", eventId);
            return;
        }

        try {
            EventHandler eventHandler = findEventHandler(event);
            if (eventHandler == null) {
                log.warn("[SettlementService.handleEvent] No handler found for event. eventId={}", eventId);
                settlementProcessedEventRepository.delete(key);
                return;
            }

            eventHandler.handle(event);

            settlementProcessedEventDbRepository.save(new SettlementProcessedEvent(eventId));
            log.info("[SettlementService.handleEvent] Event processed successfully. eventId={}", eventId);

        } catch (Exception e) {
            log.error("[SettlementService.handleEvent] Failed to handle event due to an exception. eventId={}", eventId, e);
            settlementProcessedEventRepository.delete(key);

            throw new RuntimeException("Event handling failed", e);
        }
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
