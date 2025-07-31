package crypto.order.entity.user;

import crypto.baseentity.BaseEntity;
import crypto.order.entity.order.Order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public User(String email) {
        this.email = email;
    }

    public static User createUser(String email) {
        return User.builder()
                .email(email)
                .build();
    }
}
