package crypto.order.controller.order;

import crypto.common.api.response.ApiResponse;
import crypto.order.controller.order.request.LimitOrderRequest;
import crypto.order.controller.order.request.MarketBuyOrderRequest;
import crypto.order.controller.order.request.MarketSellOrderRequest;
import crypto.order.controller.order.response.*;
import crypto.order.service.order.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders/limit/buy")
    public ApiResponse<OrderCreateResponse> createLimitBuyOrder(@RequestBody LimitOrderRequest request) {

        return ApiResponse.success(orderService.createLimitBuyOrder(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/orders/limit/sell")
    public ApiResponse<OrderCreateResponse> createLimitSellOrder(@RequestBody LimitOrderRequest request) {

        return ApiResponse.success(orderService.createLimitSellOrder(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/orders/market/buy")
    public ApiResponse<OrderCreateResponse> createMarketBuyOrder(@RequestBody MarketBuyOrderRequest request) {

        return ApiResponse.success(orderService.createMarketBuyOrder(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/orders/market/sell")
    public ApiResponse<OrderCreateResponse> createMarketSellOrder(@RequestBody MarketSellOrderRequest request) {

        return ApiResponse.success(orderService.createMarketSellOrder(request.toServiceRequest()));
    }
}
