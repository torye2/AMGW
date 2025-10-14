package amgw.amgw.repository;

import amgw.amgw.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<amgw.amgw.entity.UserEntity, Long> {
    Optional<UserEntity> findByProviderAndSubject(String provider, String subject);
}