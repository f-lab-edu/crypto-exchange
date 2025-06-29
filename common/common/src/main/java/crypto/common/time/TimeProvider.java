package crypto.common.time;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class TimeProvider {
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
