package crypto.event;

import crypto.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {

    LIMIT_BUY_ORDER_CREATE(Topic.CRYPTO_ORDER),
    LIMIT_SELL_ORDER_CREATE(Topic.CRYPTO_ORDER),
    MARKET_BUY_ORDER_CREATE(Topic.CRYPTO_ORDER),
    MARKET_SELL_ORDER_CREATE(Topic.CRYPTO_ORDER),
    TRADE_BUY_ORDER_CREATE(Topic.CRYPTO_TRADE),
    TRADE_SELL_ORDER_CREATE(Topic.CRYPTO_TRADE);

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
        public static final String CRYPTO_ORDER = "crypto-order";
        public static final String CRYPTO_TRADE = "crypto-trade";
    }
}
