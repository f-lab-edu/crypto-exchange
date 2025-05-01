package crypto.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import crypto.order.request.LimitOrderRequest;
import crypto.order.request.MarketBuyOrderRequest;
import crypto.order.request.MarketSellOrderRequest;
import crypto.order.request.OrderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("지정가 주문을 요청한다.")
    @Test
    void createLimitOrder() throws Exception {
        // given
        OrderRequest request = LimitOrderRequest.builder()
                .symbol("BTC")
                .orderType(OrderType.LIMIT)
                .orderSide(OrderSide.BUY)
                .price(5000000)
                .quantity(BigDecimal.valueOf(1.2345))
                .build();

        // when // then
        mockCreateOrderResponse(request);
    }

    @DisplayName("시장가 매수 주문을 요청한다.")
    @Test
    void createMarketBuyOrder() throws Exception {
        // given
        OrderRequest request = MarketBuyOrderRequest.builder()
                .symbol("BTC")
                .orderType(OrderType.MARKET)
                .orderSide(OrderSide.BUY)
                .totalPrice(5000000)
                .build();

        // when // then
        mockCreateOrderResponse(request);
    }

    @DisplayName("시장가 매도 주문을 요청한다.")
    @Test
    void createMarketSellOrder() throws Exception {
        // given
        OrderRequest request = MarketSellOrderRequest.builder()
                .symbol("BTC")
                .orderType(OrderType.LIMIT)
                .orderSide(OrderSide.BUY)
                .totalAmount(BigDecimal.valueOf(1.234))
                .build();

        // when // then
        mockCreateOrderResponse(request);
    }

    @DisplayName("주문 취소를 요청한다.")
    @Test
    void deleteOrder() throws Exception {
        // given
        String orderId = "abc123xyz";

        // when // then
        mockMvc.perform(
                        delete("/api/v1/orders/{orderId}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 정상적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.deletedAt").value("2025-04-30T01:00:00"));
    }

    @DisplayName("유저의 주문 가능 금액을 조회한다.")
    @Test
    void getAvailableAmount() throws Exception {
        // when // then
        mockMvc.perform(
                        get("/api/v1/orders/available")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 정상적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.currency").value("KRW"))
                .andExpect(jsonPath("$.data.amount").value("4000000"));
    }

    @DisplayName("유저의 체결 완료된 주문 목록들을 조회한다.")
    @Test
    void getCompleteOrders() throws Exception {
        // when // then
        mockMvc.perform(get("/api/v1/orders/complete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 정상적으로 처리되었습니다."))

                // content 내부 필드 검증
                .andExpect(jsonPath("$.data.content[0].orderId").value("abc123xyz"))
                .andExpect(jsonPath("$.data.content[0].symbol").value("BTC"))
                .andExpect(jsonPath("$.data.content[0].orderSide").value("BUY"))
                .andExpect(jsonPath("$.data.content[0].price").value(30000))
                .andExpect(jsonPath("$.data.content[0].amount").value(12.345))
                .andExpect(jsonPath("$.data.content[0].completedAt").value("2025-04-30T01:00:00"))

                // 페이징 정보 검증
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @DisplayName("유저의 미체결 된 주문 목록들을 조회한다.")
    @Test
    void getOpenOrders() throws Exception {
        // when // then
        mockMvc.perform(get("/api/v1/orders/open")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 정상적으로 처리되었습니다."))

                // content 내부 필드 검증
                .andExpect(jsonPath("$.data.content[0].orderId").value("abc123xyz"))
                .andExpect(jsonPath("$.data.content[0].symbol").value("BTC"))
                .andExpect(jsonPath("$.data.content[0].orderSide").value("BUY"))
                .andExpect(jsonPath("$.data.content[0].price").value(30000.00))
                .andExpect(jsonPath("$.data.content[0].requestQty").value(12.345))
                .andExpect(jsonPath("$.data.content[0].remainQty").value(12.345))
                .andExpect(jsonPath("$.data.content[0].requestedAt").value("2025-04-30T01:00:00"))

                // 페이징 정보 검증
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    private void mockCreateOrderResponse(OrderRequest request) throws Exception {
        mockMvc.perform(
                        post("/api/v1/orders")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 정상적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.orderId").value("abc123xyz"))
                .andExpect(jsonPath("$.data.createAt").value("2025-04-30T01:00:00"));
    }
}