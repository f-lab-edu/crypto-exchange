package crypto.common.fee;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class FeePolicy {

    private static final BigDecimal TAKER_FEE_RATE = new BigDecimal("0.04");
    private static final BigDecimal MAKER_FEE_RATE = new BigDecimal("0.04");

    public BigDecimal getTakerFeeRate() {
        return TAKER_FEE_RATE;
    }

    public BigDecimal getMakerFeeRate() {
        return MAKER_FEE_RATE;
    }
}
