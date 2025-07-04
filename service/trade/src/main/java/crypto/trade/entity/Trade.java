package crypto.trade.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "trades")
public class Trade {

    @Id @Column(name = "trade_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;

    private String orderSide;

    private Long takerOrderId;
    private Long makerOrderId;

    private Long takerUserId;
    private Long makerUserId;

    private BigDecimal takerFee;
    private BigDecimal makerFee;

    private LocalDateTime tradedAt;

    @Builder
    public Trade(String symbol, BigDecimal price, BigDecimal quantity, String orderSide, Long takerOrderId, Long makerOrderId,
                 Long takerUserId, Long makerUserId, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime tradedAt) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.orderSide = orderSide;
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.takerUserId = takerUserId;
        this.makerUserId = makerUserId;
        this.takerFee = takerFee;
        this.makerFee = makerFee;
        this.tradedAt = tradedAt;
    }

    public static Trade create(String symbol, BigDecimal price, BigDecimal quantity, String orderSide, Long takerOrderId,
                               Long makerOrderId, Long takerUserId, Long makerUserId, BigDecimal takerFee, BigDecimal makerFee, LocalDateTime tradedAt) {
        return Trade.builder()
                .symbol(symbol)
                .price(price)
                .quantity(quantity)
                .orderSide(orderSide)
                .takerOrderId(takerOrderId)
                .makerOrderId(makerOrderId)
                .takerUserId(takerUserId)
                .makerUserId(makerUserId)
                .takerFee(takerFee)
                .makerFee(makerFee)
                .tradedAt(tradedAt)
                .build();
    }
}
