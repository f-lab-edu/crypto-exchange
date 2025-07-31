package crypto.settlement.service;

import crypto.common.security.context.UserContext;
import crypto.settlement.controller.response.CheckBalanceResponse;
import crypto.settlement.controller.response.CheckQuantityResponse;
import crypto.settlement.service.exception.SettlementServiceException;
import crypto.settlement.service.request.CheckBalanceServiceRequest;
import crypto.settlement.service.request.CheckQuantityServiceRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final RestClient restClient;

    public CheckBalanceResponse checkBalance(CheckBalanceServiceRequest request) {
        Long userId = UserContext.getUserId();
        try {
            return restClient.post()
                    .uri("api/v1/settlement/check-balance")
                    .header("X-UID", userId.toString())
                    .body(request)
                    .retrieve()
                    .body(CheckBalanceResponse.class);
        } catch (Exception e) {
            log.error("[SettlementService.checkBalance] RestClient error for userId={}: {}", userId, e.getMessage(), e);
            throw new SettlementServiceException();
        }
    }

    public CheckQuantityResponse checkQuantity(CheckQuantityServiceRequest request) {
        Long userId = UserContext.getUserId();
        try {
            return restClient.post()
                    .uri("api/v1/settlement/check-quantity")
                    .header("X-UID", userId.toString())
                    .body(request)
                    .retrieve()
                    .body(CheckQuantityResponse.class);
        } catch (Exception e) {
            log.error("[SettlementService.checkQuantity] RestClient error for userId={}: {}", userId, e.getMessage(), e);
            throw new SettlementServiceException();
        }
    }

}
