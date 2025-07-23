package crypto.order.controller.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import crypto.common.api.response.ApiResponse;
import crypto.common.security.config.SecurityConfig;
import crypto.order.controller.order.request.LimitOrderRequest;
import crypto.order.controller.order.request.MarketBuyOrderRequest;
import crypto.order.controller.order.request.MarketSellOrderRequest;
import crypto.order.controller.order.response.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.math.BigDecimal;

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
}