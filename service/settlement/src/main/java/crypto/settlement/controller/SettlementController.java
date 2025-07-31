package crypto.settlement.controller;

import crypto.settlement.controller.request.CheckBalanceRequest;
import crypto.settlement.controller.request.CheckQuantityRequest;
import crypto.settlement.controller.response.CheckBalanceResponse;
import crypto.settlement.controller.response.CheckQuantityResponse;
import crypto.settlement.service.SettlementService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/check-balance")
    public CheckBalanceResponse checkUserBalance(@RequestBody CheckBalanceRequest request) {

        return settlementService.checkBalance(request.toServiceRequest());
    }

    @PostMapping("/check-quantity")
    public CheckQuantityResponse checkUserBalance(@RequestBody CheckQuantityRequest request) {

        return settlementService.checkQuantity(request.toServiceRequest());
    }
}
