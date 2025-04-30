package crypto.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import crypto.PageResponse;
import crypto.order.request.LimitOrderRequest;
import crypto.order.request.MarketBuyOrderRequest;
import crypto.order.request.MarketSellOrderRequest;
import crypto.ApiResponse;
import crypto.order.request.OrderRequest;
import crypto.order.response.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static crypto.order.OrderSide.*;

@RequiredArgsConstructor
@RestController
public class OrderController {

    private final ObjectMapper objectMapper;

    @PostMapping("/api/v1/orders")
    public ApiResponse<OrderCreateResponse> createOrder(@RequestBody Map<String, Object> rawJson) {
        String orderType = (String) rawJson.get("orderType");
        String orderSide = (String) rawJson.get("orderSide");
        OrderRequest request;

        if ("LIMIT".equals(orderType)) {
            request = objectMapper.convertValue(rawJson, LimitOrderRequest.class);
        } else if ("MARKET".equals(orderType) && "BUY".equals(orderSide)) {
            request = objectMapper.convertValue(rawJson, MarketBuyOrderRequest.class);
        } else if ("MARKET".equals(orderType) && "SELL".equals(orderSide)) {
            request = objectMapper.convertValue(rawJson, MarketSellOrderRequest.class);
        } else {
            throw new IllegalArgumentException("지원하지 않는 주문 타입입니다");
        }

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

    @GetMapping("/api/v1/orders")
    public ApiResponse<PageResponse<CompleteOrderListResponse>> getOrders(
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
                .build();

        List<CompleteOrderListResponse> content = List.of(response);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<CompleteOrderListResponse> pageResult = new PageImpl<>(content, pageRequest, content.size());

        return ApiResponse.successPage(pageResult);
    }

    @GetMapping("/api/v1/orders/open")
    public ApiResponse<Page<OpenOrderListResponse>> getOpenOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol
    ) {
        return null;
    }
}
