package crypto.order;

import crypto.order.request.LimitOrderRequest;
import crypto.order.request.MarketBuyOrderRequest;
import crypto.order.request.MarketSellOrderRequest;
import crypto.response.PageResponse;
import crypto.response.ApiResponse;
import crypto.order.response.*;

import crypto.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static crypto.order.OrderSide.*;


@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders/limit/buy")
    public ApiResponse<OrderCreateResponse> createLimitBuyOrder(@RequestBody LimitOrderRequest request) {
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Order order = orderService.createLimitBuyOrder(request.toServiceRequest(), registeredDateTime);

        return ApiResponse.success(OrderCreateResponse.of(order));
    }

    @PostMapping("/api/v1/orders/limit/sell")
    public ApiResponse<OrderCreateResponse> createLimitSellOrder(@RequestBody LimitOrderRequest request) {
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Order order = orderService.createLimitSellOrder(request.toServiceRequest(), registeredDateTime);

        return ApiResponse.success(OrderCreateResponse.of(order));
    }

    @PostMapping("/api/v1/orders/market/buy")
    public ApiResponse<OrderCreateResponse> createMarketBuyOrder(@RequestBody MarketBuyOrderRequest request) {
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Order order = orderService.createMarketBuyOrder(request.toServiceRequest(), registeredDateTime);

        return ApiResponse.success(OrderCreateResponse.of(order));
    }

    @PostMapping("/api/v1/orders/market/sell")
    public ApiResponse<OrderCreateResponse> createMarketSellOrder(@RequestBody MarketSellOrderRequest request) {
        LocalDateTime registeredDatetime = LocalDateTime.now();
        Order order = orderService.createMarketSellOrder(request.toServiceRequest(), registeredDatetime);

        return ApiResponse.success(OrderCreateResponse.of(order));
    }

    @DeleteMapping("/api/v1/orders/{orderId}")
    public ApiResponse<OrderDeleteResponse> deleteOrder(@PathVariable Long orderId) {
        LocalDateTime deletedDateTime = LocalDateTime.now();
        Order order = orderService.deleteOrder(orderId, deletedDateTime);

        return ApiResponse.success(OrderDeleteResponse.of(order));
    }

    @GetMapping("/api/v1/orders/available")
    public ApiResponse<OrderAvailableResponse> getAvailableAmount() {
        User user = orderService.getUser();

        return ApiResponse.success(OrderAvailableResponse.of(user));
    }

    @GetMapping("/api/v1/orders/complete")
    public ApiResponse<PageResponse<CompleteOrderListResponse>> getCompleteOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CompleteOrderListResponse response = CompleteOrderListResponse.builder()
                .orderId(1234L)
                .symbol("BTC")
                .orderSide(BUY)
                .price(BigDecimal.valueOf(30000))
                .amount(BigDecimal.valueOf(12.345))
                .completedAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        Page<CompleteOrderListResponse> pageResult = createPageResult(response, page, size);

        return ApiResponse.successPage(pageResult);
    }

    @GetMapping("/api/v1/orders/open")
    public ApiResponse<PageResponse<OpenOrderListResponse>> getOpenOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        OpenOrderListResponse response = OpenOrderListResponse.builder()
                .orderId(1234L)
                .symbol("BTC")
                .orderSide(BUY)
                .price(BigDecimal.valueOf(30000.00))
                .requestQty(BigDecimal.valueOf(12.345))
                .remainQty(BigDecimal.valueOf(12.345))
                .requestedAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        Page<OpenOrderListResponse> pageResult = createPageResult(response, page, size);

        return ApiResponse.successPage(pageResult);
    }

    public <T> Page<T> createPageResult(T response, int page, int size) {
        List<T> content = List.of(response);
        PageRequest pageRequest = PageRequest.of(page, size);
        return new PageImpl<>(content, pageRequest, content.size());
    }
}
