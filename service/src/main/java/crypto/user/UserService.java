package crypto.user;

import crypto.context.UserContext;
import crypto.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
