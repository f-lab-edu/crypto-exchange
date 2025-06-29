package crypto.order.repository.user;

import crypto.order.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}
