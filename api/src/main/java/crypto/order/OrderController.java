package crypto.order;

import crypto.response.PageResponse;
import crypto.response.ApiResponse;
import crypto.order.response.*;

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

    @PostMapping("/api/v1/orders/limit/buy")
    public ApiResponse<OrderCreateResponse> createLimitBuyOrder() {
        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId("abc123xyz")
                .createAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/api/v1/orders/limit/sell")
    public ApiResponse<OrderCreateResponse> createLimitSellOrder() {
        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId("abc123xyz")
                .createAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/api/v1/orders/market/buy")
    public ApiResponse<OrderCreateResponse> createMarketBuyOrder() {
        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId("abc123xyz")
                .createAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/api/v1/orders/market/sell")
    public ApiResponse<OrderCreateResponse> createMarketSellOrder() {
        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId("abc123xyz")
                .createAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/orders/{orderId}")
    public ApiResponse<OrderDeleteResponse> deleteOrder(@PathVariable String orderId) {
        OrderDeleteResponse response = OrderDeleteResponse.builder()
                .orderId(orderId)
                .deletedAt(LocalDateTime.of(2025, 4, 30, 1, 0))
                .build();

        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/orders/available")
    public ApiResponse<OrderAvailableResponse> getAvailableAmount() {
        OrderAvailableResponse response = OrderAvailableResponse.builder()
                .currency("KRW")
                .amount(4000000)
                .build();

        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/orders/complete")
    public ApiResponse<PageResponse<CompleteOrderListResponse>> getCompleteOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol
    ) {
        CompleteOrderListResponse response = CompleteOrderListResponse.builder()
                .orderId("abc123xyz")
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol
    ) {
        OpenOrderListResponse response = OpenOrderListResponse.builder()
                .orderId("abc123xyz")
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
