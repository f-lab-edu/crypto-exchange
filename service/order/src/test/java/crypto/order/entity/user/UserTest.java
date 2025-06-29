package crypto.order.entity.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles("test")
@Transactional
class UserTest {

    @DisplayName("유저 생성시 초기 lockedBalance 는 0이다.")
    @Test
    void checkUserLockedBalance() {
        // given
        User user = User.createUser("test@email.com");

        // when // then
        assertThat(user.getUserBalance().getLockedBalance()).isEqualByComparingTo(valueOf(0));
    }

    @DisplayName("매수 주문시 주문 요청 가격만큼 해당 유저의 잔고가 잠금처리되고 사용 가능 금액이 차감된다.")
    @Test
    void checkIncreaseLockBalance() {
        // given
        User user = User.createUser("test@email.com");
        UserBalance userBalance = user.getUserBalance();
        userBalance.increaseAvailableBalance(valueOf(1000));
        BigDecimal orderTotalPrice = valueOf(100);

        // when
        userBalance.increaseLockedBalance(orderTotalPrice);

        // then
        assertThat(userBalance.getLockedBalance()).isEqualByComparingTo(valueOf(100));
        assertThat(userBalance.getAvailableBalance()).isEqualByComparingTo(valueOf(900));
    }

    @DisplayName("주문 요청 가격이 유저의 사용가능 금액보다 큰 경우 예외가 발생한다.")
    @Test
    void checkOrderPriceExceedsAvailableBalance() {
        // given
        User user = User.createUser("test@email.com");
        UserBalance userBalance = user.getUserBalance();
        userBalance.increaseAvailableBalance(valueOf(1000));
        BigDecimal orderTotalPrice = valueOf(1100);

        // when // then
        assertThatThrownBy(() -> userBalance.increaseLockedBalance(orderTotalPrice))
                .isInstanceOf(InsufficientAvailableBalanceException.class)
                .hasMessage("사용가능한 잔액이 부족합니다.");
    }

    @DisplayName("매수 주문 정산 이후 잠금 처리 되었던 잔고가 복구된다.")
    @Test
    void checkUnlockBalance() {
        // given
        User user = User.createUser("test@email.com");
        UserBalance userBalance = user.getUserBalance();
        userBalance.increaseAvailableBalance(valueOf(1000));
        BigDecimal orderTotalPrice = valueOf(500);

        userBalance.increaseLockedBalance(orderTotalPrice);

        // when
        userBalance.decreaseLockedBalance(orderTotalPrice);

        // then
        assertThat(userBalance.getLockedBalance()).isEqualByComparingTo(valueOf(0));
    }

    @DisplayName("잠금 처리 되었던 잔고보다 큰 금액이 정산되는 경우 예외가 발생한다.")
    @Test
    void checkSettlePriceExceedsLockedBalance() {
        // given
        User user = User.createUser("test@email.com");
        UserBalance userBalance = user.getUserBalance();
        userBalance.increaseAvailableBalance(valueOf(1000));
        BigDecimal orderTotalPrice = valueOf(500);

        userBalance.increaseLockedBalance(orderTotalPrice);
        BigDecimal settlePrice = valueOf(600);

        // when // then
        assertThatThrownBy(() -> userBalance.decreaseLockedBalance(settlePrice))
                .isInstanceOf(LockedBalanceExceedException.class)
                .hasMessage("잠금처리된 잔액 보다 큰 금액입니다.");
    }

    @DisplayName("매도 주문 정산 이후 사용 가능 금액이 증가한다.")
    @Test
    void checkAddAvailableBalance() {
        // given
        User user = User.createUser("test@email.com");
        UserBalance userBalance = user.getUserBalance();
        userBalance.increaseAvailableBalance(valueOf(1000));
        BigDecimal orderTotalPrice = valueOf(500);

        // when
        userBalance.increaseAvailableBalance(orderTotalPrice);

        // then
        assertThat(userBalance.getAvailableBalance()).isEqualByComparingTo(valueOf(1500));
    }
}