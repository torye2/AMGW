package amgw.amgw.repository;

import amgw.amgw.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //Optional<UserEntity> findByProviderAndSubject(String provider, String subject);
    Optional<User> findByUsername(String username);
}