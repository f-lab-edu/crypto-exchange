package crypto.order;

import crypto.order.request.OrderRequest;
import crypto.ApiResponse;
import crypto.order.response.OpenOrderResponse;
import crypto.order.response.OrderAvailableResponse;
import crypto.order.response.OrderCreateResponse;
import crypto.order.response.OrderDeleteResponse;


import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


public class OrderController {

    @PostMapping("/api/v1/orders")
    public ApiResponse<OrderCreateResponse> orderCreate(@RequestBody OrderRequest orderRequest) {
        return null;
    }

    @DeleteMapping("/api/v1/{orderId}")
    public ApiResponse<OrderDeleteResponse> orderDelete(@PathVariable String orderId) {
        return null;
    }

    @GetMapping("/api/v1/orders/available")
    public ApiResponse<OrderAvailableResponse> getOrderAvailable() {
        return null;
    }

    @GetMapping("/api/v1/orders")
    public ApiResponse<Page<OpenOrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol
    ) {
        return null;
    }

    @GetMapping("/api/v1/orders/open")
    public ApiResponse<Page<OpenOrderResponse>> getOpenOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol
    ) {
        return null;
    }
}
