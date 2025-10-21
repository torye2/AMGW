package amgw.amgw.repository;

import amgw.amgw.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //Optional<UserEntity> findByProviderAndSubject(String provider, String subject);
    Optional<User> findByUsername(String username);

    @Query("select u.username from User u where u.id = :userId")
    String findUsernameById(@Param("userId") Long userId);

    @Query("select u.name from User u where u.id = :userId")
    String findNameById(@Param("userId") Long userId);
}