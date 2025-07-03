package crypto.event;

import crypto.event.exception.MatchingTypeNotExistException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {

    LIMIT_BUY_ORDER_CREATE(Topic.CRYPTO_ORDER, "LIMIT_BUY_ORDER_TRADE"),
    LIMIT_SELL_ORDER_CREATE(Topic.CRYPTO_ORDER, "LIMIT_SELL_ORDER_TRADE"),
    MARKET_BUY_ORDER_CREATE(Topic.CRYPTO_ORDER, "MARKET_BUY_ORDER_TRADE"),
    MARKET_SELL_ORDER_CREATE(Topic.CRYPTO_ORDER, "MARKET_SELL_ORDER_TRADE"),

    LIMIT_BUY_ORDER_TRADE(Topic.CRYPTO_TRADE, null),
    LIMIT_SELL_ORDER_TRADE(Topic.CRYPTO_TRADE, null),
    MARKET_BUY_ORDER_TRADE(Topic.CRYPTO_TRADE, null),
    MARKET_SELL_ORDER_TRADE(Topic.CRYPTO_TRADE, null),

    BUY_ORDER_SETTLEMENT(Topic.CRYPTO_SETTLEMENT, null),
    SELL_ORDER_SETTLEMENT(Topic.CRYPTO_SETTLEMENT, null),
    REFUND_LOCKED_BALANCE(Topic.CRYPTO_SETTLEMENT, null),

    FAIL_ORDER_EVENT(Topic.CRYPTO_ORDER_DLQ, null),
    FAIL_TRADE_EVENT(Topic.CRYPTO_TRADE_DLQ, null);

    private final String topic;
    private final String matchingTypeName;

    public EventType toMatchingEventType() {
        if (this.matchingTypeName == null) {
            throw new MatchingTypeNotExistException();
        }
        return EventType.valueOf(this.matchingTypeName);
    }

    public static class Topic {
        public static final String CRYPTO_ORDER = "crypto-order";
        public static final String CRYPTO_TRADE = "crypto-trade";
        public static final String CRYPTO_SETTLEMENT = "crypto-settlement";
        public static final String CRYPTO_ORDER_DLQ = "crypto-order-dlq";
        public static final String CRYPTO_TRADE_DLQ = "crypto-trade-dlq";
        public static final String CRYPTO_SETTLEMENT_DLQ = "crypto-settlement-dlq";
    }
}
