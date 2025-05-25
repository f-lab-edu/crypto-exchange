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

    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private BigDecimal lockedBalance;

    private LocalDateTime registeredDateTime;
    private LocalDateTime deletedDateTime;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public User(String email, BigDecimal totalBalance, BigDecimal availableBalance) {
        this.email = email;
        this.totalBalance = totalBalance;
        this.availableBalance = availableBalance;
    }

    public static User createUser(String email, BigDecimal totalBalance, BigDecimal availableBalance) {
        return User.builder()
                .email(email)
                .totalBalance(totalBalance)
                .availableBalance(availableBalance)
                .build();
    }
}
