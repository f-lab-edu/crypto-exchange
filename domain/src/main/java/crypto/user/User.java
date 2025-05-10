package crypto.user;

import crypto.BaseEntity;
import crypto.order.Order;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
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
    public User(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}
