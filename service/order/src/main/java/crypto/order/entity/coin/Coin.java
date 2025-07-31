package crypto.order.entity.coin;

import crypto.baseentity.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coins")
public class Coin extends BaseEntity {

    @Id @Column(name = "coin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String symbol;

    private String name;

    private boolean isActive = true;

    @Builder
    public Coin(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public static Coin create(String symbol, String name) {
        return Coin.builder()
                .symbol(symbol)
                .name(name)
                .build();
    }
}

