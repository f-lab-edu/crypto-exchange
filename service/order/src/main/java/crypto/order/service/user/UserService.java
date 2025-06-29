package crypto.order.service.user;

import crypto.common.security.context.UserContext;

import crypto.order.entity.user.User;
import crypto.order.repository.user.UserRepository;
import crypto.order.service.user.exception.UserNotFoundException;
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
