package crypto.settlementdata.entity;

import crypto.settlementdata.entity.exception.InsufficientAvailableBalanceException;
import crypto.settlementdata.entity.exception.LockedBalanceExceedException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

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

    @Column(unique = true)
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
