package crypto.trade.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;


@Repository
@RequiredArgsConstructor
public class TradeProcessedEventRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "trade::event::%s::event-id";
    private static final String PROCESSED_VALUE = "1";
    private static final Duration TTL = Duration.ofDays(7);

    public Boolean setIfAbsent(String eventId) {
        String key = generateKey(eventId);
        return redisTemplate.opsForValue().setIfAbsent(key, PROCESSED_VALUE, TTL);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public String generateKey(String eventId) {
        return KEY_FORMAT.formatted(eventId);
    }
}
