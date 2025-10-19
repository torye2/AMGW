package amgw.amgw.repository;

import amgw.amgw.entity.EmailVerificationToken;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, Long> {
    @Query("""
        SELECT t FROM EmailVerificationToken t
        WHERE t.token = :token
          AND t.purpose = :purpose
          AND t.usedAt IS NULL
          AND t.expiresAt > :NOW
    """)
    Optional<EmailVerificationToken> findValid(@Param("token") String token,
                                               @Param("purpose") String purpose,
                                               LocalDateTime now);
}

