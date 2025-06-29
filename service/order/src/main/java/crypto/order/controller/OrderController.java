package crypto.order.controller;

import crypto.common.api.response.ApiResponse;
import crypto.common.api.response.PageResponse;
import crypto.order.controller.request.LimitOrderRequest;
import crypto.order.controller.request.MarketBuyOrderRequest;
import crypto.order.controller.request.MarketSellOrderRequest;
import crypto.order.controller.response.*;
import crypto.order.service.order.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @DeleteMapping("/api/v1/orders/{orderId}")
    public ApiResponse<OrderDeleteResponse> deleteOrder(@PathVariable Long orderId) {

        return ApiResponse.success(orderService.deleteOrder(orderId));
    }

    @GetMapping("/api/v1/orders/available")
    public ApiResponse<OrderAvailableResponse> getAvailableAmount() {

        return ApiResponse.success(orderService.getAvailableAmount());
    }

    @GetMapping("/api/v1/orders/complete")
    public ApiResponse<PageResponse<CompleteOrderListResponse>> getCompleteOrders(
            @PageableDefault(size = 10, sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable)
    {
        Page<CompleteOrderListResponse> completeOrders = orderService.getCompleteOrders(pageable);

        return ApiResponse.successPage(completeOrders);
    }

    @GetMapping("/api/v1/orders/open")
    public ApiResponse<PageResponse<OpenOrderListResponse>> getOpenOrders(
            @PageableDefault(size = 10, sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable)
    {
        Page<OpenOrderListResponse> openOrders = orderService.getOpenOrders(pageable);

        return ApiResponse.successPage(openOrders);
    }
}
