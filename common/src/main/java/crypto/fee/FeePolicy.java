package crypto.fee;

import java.math.BigDecimal;


public interface FeePolicy {
    BigDecimal getTakerFeeRate();
    BigDecimal getMakerFeeRate();
}

