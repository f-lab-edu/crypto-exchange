package crypto.settlement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_coins")
public class UserCoin {

    @Id @Column(name = "user_coin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String symbol;

    private BigDecimal availableQuantity = BigDecimal.ZERO;
    private BigDecimal lockedQuantity = BigDecimal.ZERO;

    @Builder
    public UserCoin(Long userId, String symbol) {
        this.userId = userId;
        this.symbol = symbol;
    }

    public static UserCoin create(Long userId, String symbol) {
        return UserCoin.builder()
                .userId(userId)
                .symbol(symbol)
                .build();
    }

    public void increaseQuantity(BigDecimal quantity) {
        this.availableQuantity = this.availableQuantity.add(quantity);
    }

    public void increaseLockedQuantity(BigDecimal quantity) {
        this.lockedQuantity = this.lockedQuantity.add(quantity);
        this.availableQuantity = this.availableQuantity.subtract(quantity);
    }

    public void decreaseLockQuantity(BigDecimal quantity) {
        this.lockedQuantity = this.lockedQuantity.subtract(quantity);
    }
}

