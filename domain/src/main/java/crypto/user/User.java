package crypto.user;

import crypto.BaseEntity;
import crypto.order.Order;
import crypto.user.exception.InsufficientAvailableBalanceException;
import crypto.user.exception.LockedBalanceExceedException;
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

    private LocalDateTime registeredDateTime;
    private LocalDateTime deletedDateTime;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false)
    private UserBalance userBalance;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public User(String email, UserBalance userBalance) {
        this.email = email;
        this.userBalance = userBalance;
    }

    public static User createUser(String email) {
        UserBalance userBalance = new UserBalance("KRW");
        User user = User.builder()
                .email(email)
                .userBalance(userBalance)
                .build();

        userBalance.setUser(user);

        return user;
    }
}
