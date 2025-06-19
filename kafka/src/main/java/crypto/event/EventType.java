package crypto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {

    LIMIT_BUY_ORDER_CREATE(Topic.CRYPTO_LIMIT_ORDER),
    LIMIT_SELL_ORDER_CREATE(Topic.CRYPTO_LIMIT_ORDER),
    MARKET_BUY_ORDER_CREATE(Topic.CRYPTO_MARKET_ORDER),
    MARKET_SELL_ORDER_CREATE(Topic.CRYPTO_MARKET_ORDER),
    TRADE_BUY_ORDER_CREATE(Topic.CRYPTO_TRADE),
    TRADE_SELL_ORDER_CREATE(Topic.CRYPTO_TRADE),
    FAIL_ORDER_EVENT(Topic.CRYPTO_ORDER_DLQ),
    FAIL_TRADE_EVENT(Topic.CRYPTO_TRADE_DLQ);

    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String CRYPTO_LIMIT_ORDER = "crypto-limit-order";
        public static final String CRYPTO_MARKET_ORDER = "crypto-market-order";
        public static final String CRYPTO_TRADE = "crypto-trade";
        public static final String CRYPTO_ORDER_DLQ = "crypto-order-dlq";
        public static final String CRYPTO_TRADE_DLQ = "crypto-trade-dlq";
    }
}
