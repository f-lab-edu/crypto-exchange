package crypto.order.controller.order.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class OrderCreateResponse {

    private LocalDateTime createdAt;

    @Builder
    public OrderCreateResponse(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static OrderCreateResponse of(LocalDateTime createdAt) {
        return OrderCreateResponse.builder()
                .createdAt(createdAt)
                .build();
    }
}
