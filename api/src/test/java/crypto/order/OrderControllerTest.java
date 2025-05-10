package crypto.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import crypto.order.request.*;
import crypto.order.response.*;
import crypto.response.ApiResponse;
import crypto.config.SecurityConfig;

import crypto.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.math.BigDecimal;

import static crypto.order.OrderSide.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("지정가 구매 주문을 요청한다.")
    @Test
    void createLimitBuyOrder() throws Exception {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-UID", "42");

        LimitOrderRequest request = LimitOrderRequest.builder()
                .symbol("BTC")
                .price(BigDecimal.valueOf(50000))
                .quantity(BigDecimal.valueOf(1.2345))
                .build();

        HttpEntity<LimitOrderRequest> requestEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders/limit/buy",
                requestEntity,
                String.class
        );

        // then
        ApiResponse<OrderCreateResponse> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<OrderCreateResponse>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");
    }

    @DisplayName("지정가 판매 주문을 요청한다.")
    @Test
    void createLimitSellOrder() throws Exception {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-UID", "42");

        LimitOrderRequest request = LimitOrderRequest.builder()
                .symbol("BTC")
                .price(BigDecimal.valueOf(500000))
                .quantity(BigDecimal.valueOf(1.2345))
                .build();

        HttpEntity<LimitOrderRequest> requestEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders/limit/sell",
                requestEntity,
                String.class
        );

        // then
        ApiResponse<OrderCreateResponse> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<OrderCreateResponse>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");
    }

    @DisplayName("시장가 매수 주문을 요청한다.")
    @Test
    void createMarketBuyOrder() throws Exception {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-UID", "42");

        MarketBuyOrderRequest request = MarketBuyOrderRequest.builder()
                .symbol("BTC")
                .totalPrice(BigDecimal.valueOf(50000))
                .build();

        HttpEntity<MarketBuyOrderRequest> requestEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders/market/buy",
                requestEntity,
                String.class
        );

        // then
        ApiResponse<OrderCreateResponse> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<OrderCreateResponse>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");
    }

    @DisplayName("시장가 매도 주문을 요청한다.")
    @Test
    void createMarketSellOrder() throws Exception {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-UID", "42");

        MarketSellOrderRequest request = MarketSellOrderRequest.builder()
                .symbol("BTC")
                .totalAmount(BigDecimal.valueOf(1.234))
                .build();

        HttpEntity<MarketSellOrderRequest> requestEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders/market/sell",
                requestEntity,
                String.class
        );

        // then
        ApiResponse<OrderCreateResponse> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<OrderCreateResponse>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");
    }

    @DisplayName("주문 취소를 요청한다.")
    @Test
    void deleteOrder() throws Exception {
        // given
        String orderId = "1234";

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/orders/{orderId}",
                HttpMethod.DELETE,
                null,
                String.class,
                orderId
        );

        // then
        ApiResponse<OrderDeleteResponse> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<OrderDeleteResponse>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");
    }

    @DisplayName("유저의 주문 가능 금액을 조회한다.")
    @Test
    void getAvailableAmount() throws Exception {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-UID", "42");
        HttpEntity<MarketSellOrderRequest> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/orders/available",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        // then
        ApiResponse<OrderAvailableResponse> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<OrderAvailableResponse>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");
    }

    @DisplayName("유저의 체결 완료된 주문 목록들을 조회한다.")
    @Test
    void getCompleteOrders() throws Exception {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/orders/complete",
                String.class
        );

        // then
        ApiResponse<PageResponse<CompleteOrderListResponse>> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<PageResponse<CompleteOrderListResponse>>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");

        assertThat(apiResponse.getData().getContent().getFirst().getOrderId()).isEqualTo(1234L);
        assertThat(apiResponse.getData().getContent().getFirst().getSymbol()).isEqualTo("BTC");
        assertThat(apiResponse.getData().getContent().getFirst().getOrderSide()).isEqualTo(BUY);
        assertThat(apiResponse.getData().getContent().getFirst().getPrice()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(apiResponse.getData().getContent().getFirst().getAmount()).isEqualTo(BigDecimal.valueOf(12.345));
        assertThat(apiResponse.getData().getContent().getFirst().getCompletedAt()).isEqualTo("2025-04-30T01:00:00");

        assertThat(apiResponse.getData().getTotalElements()).isEqualTo(1);
        assertThat(apiResponse.getData().getTotalPages()).isEqualTo(1);
        assertThat(apiResponse.getData().getSize()).isEqualTo(10);
        assertThat(apiResponse.getData().getNumber()).isEqualTo(0);
    }

    @DisplayName("유저의 미체결 된 주문 목록들을 조회한다.")
    @Test
    void getOpenOrders() throws Exception {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/orders/open",
                String.class
        );

        // then
        ApiResponse<PageResponse<OpenOrderListResponse>> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<PageResponse<OpenOrderListResponse>>>() {}
        );

        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(apiResponse.getMessage()).isEqualTo("요청이 정상적으로 처리되었습니다.");

        assertThat(apiResponse.getData().getContent().getFirst().getOrderId()).isEqualTo(1234L);
        assertThat(apiResponse.getData().getContent().getFirst().getSymbol()).isEqualTo("BTC");
        assertThat(apiResponse.getData().getContent().getFirst().getOrderSide()).isEqualTo(BUY);
        assertThat(apiResponse.getData().getContent().getFirst().getPrice()).isEqualTo(BigDecimal.valueOf(30000.00));
        assertThat(apiResponse.getData().getContent().getFirst().getRequestQty()).isEqualTo(BigDecimal.valueOf(12.345));
        assertThat(apiResponse.getData().getContent().getFirst().getRemainQty()).isEqualTo(BigDecimal.valueOf(12.345));
        assertThat(apiResponse.getData().getContent().getFirst().getRequestedAt()).isEqualTo("2025-04-30T01:00:00");

        assertThat(apiResponse.getData().getTotalElements()).isEqualTo(1);
        assertThat(apiResponse.getData().getTotalPages()).isEqualTo(1);
        assertThat(apiResponse.getData().getSize()).isEqualTo(10);
        assertThat(apiResponse.getData().getNumber()).isEqualTo(0);
    }
}