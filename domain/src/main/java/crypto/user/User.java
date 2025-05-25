package crypto.user;

import crypto.BaseEntity;
import crypto.order.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Entity
public class User extends BaseEntity {

    @Id @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;
    private String appPassword;
    private String phoneNumber;

    private BigDecimal availableBalance = BigDecimal.ZERO;
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    private LocalDateTime registeredDateTime;
    private LocalDateTime deletedDateTime;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public User(String email, BigDecimal availableBalance) {
        this.email = email;
        this.availableBalance = availableBalance;
    }

    public static User createUser(String email, BigDecimal availableBalance) {
        return User.builder()
                .email(email)
                .availableBalance(availableBalance)
                .build();
    }

    public void increaseLockedBalance(BigDecimal price) {
        this.lockedBalance = this.lockedBalance.add(price);
        this.availableBalance = this.availableBalance.subtract(price);
    }

    public void decreaseLockedBalance(BigDecimal price) {
        this.lockedBalance = this.lockedBalance.subtract(price);
        this.availableBalance = this.availableBalance.add(price);

    public void buyOrderSettlement(BigDecimal price) {
        this.lockedBalance = this.lockedBalance.subtract(price);
    }

    public void sellOrderSettlement(BigDecimal price) {
        this.availableBalance = this.availableBalance.add(price);
    }

    public void feeSettlement(BigDecimal price) {
        this.availableBalance = this.availableBalance.subtract(price);
    }
}
