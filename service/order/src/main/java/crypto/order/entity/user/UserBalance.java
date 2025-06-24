package crypto.order.entity.user;

import crypto.order.entity.user.exception.InsufficientAvailableBalanceException;
import crypto.order.entity.user.exception.LockedBalanceExceedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBalance {

    @Id @Column(name = "user_balance_id")
    @GeneratedValue
    private Long id;

    private Long userId;

    private String currency;

    private BigDecimal availableBalance = BigDecimal.ZERO;
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    public UserBalance(String currency) {
        this.currency = currency;
    }

    public void increaseAvailableBalance(BigDecimal price) {
        this.availableBalance = this.availableBalance.add(price);
    }

    public void increaseLockedBalance(BigDecimal price) {
        if (this.availableBalance.compareTo(price) < 0) {
            throw new InsufficientAvailableBalanceException();
        }

        this.lockedBalance = this.lockedBalance.add(price);
        this.availableBalance = this.availableBalance.subtract(price);
    }

    public void decreaseLockedBalance(BigDecimal price) {
        if (this.lockedBalance.compareTo(price) < 0) {
            throw new LockedBalanceExceedException();
        }

        this.lockedBalance = this.lockedBalance.subtract(price);
    }
}
