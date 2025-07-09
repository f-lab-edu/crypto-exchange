package crypto.wallet.controller;

import crypto.wallet.controller.request.CheckBalanceRequest;
import crypto.wallet.controller.request.CheckQuantityRequest;
import crypto.wallet.controller.response.CheckBalanceResponse;
import crypto.wallet.controller.response.CheckQuantityResponse;
import crypto.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/check-balance")
    public CheckBalanceResponse checkUserBalance(@RequestBody CheckBalanceRequest request) {

        return walletService.checkBalance(request.toServiceRequest());
    }

    @PostMapping("/check-quantity")
    public CheckQuantityResponse checkUserBalance(@RequestBody CheckQuantityRequest request) {

        return walletService.checkQuantity(request.toServiceRequest());
    }

}
