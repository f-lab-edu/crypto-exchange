package crypto.common.security.context;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class UserContext {
    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return Long.valueOf((String) auth.getPrincipal());
    }
}

