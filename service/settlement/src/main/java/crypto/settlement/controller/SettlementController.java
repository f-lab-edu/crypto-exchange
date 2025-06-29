package crypto.settlement.controller;

import crypto.common.api.response.ApiResponse;
import crypto.settlement.controller.request.CheckBalanceRequest;
import crypto.settlement.controller.request.CheckQuantityRequest;
import crypto.settlement.controller.response.CheckBalanceResponse;
import crypto.settlement.controller.response.CheckQuantityResponse;
import crypto.settlement.service.SettlementService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/api/v1/settlement/check-balance")
    public ApiResponse<CheckBalanceResponse> checkUserBalance(@RequestBody CheckBalanceRequest request) {

        return ApiResponse.success(settlementService.checkBalance(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/settlement/check-quantity")
    public ApiResponse<CheckQuantityResponse> checkUserBalance(@RequestBody CheckQuantityRequest request) {

        return ApiResponse.success(settlementService.checkQuantity(request.toServiceRequest()));
    }

}
