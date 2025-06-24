package crypto.order.entity.user;

import crypto.order.entity.coin.Coin;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id")
    private Coin coin;

    private BigDecimal availableQuantity = BigDecimal.ZERO;
    private BigDecimal lockedQuantity = BigDecimal.ZERO;

    @Builder
    public UserCoin(User user, Coin coin, BigDecimal quantity) {
        this.user = user;
        this.coin = coin;
    }

    public static UserCoin create(User user, Coin coin) {
        return UserCoin.builder()
                .user(user)
                .coin(coin)
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

