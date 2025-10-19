package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens",
        indexes = {
                @Index(name="idx_user_purpose_exp", columnList="user_id,purpose,expires_at"),
                @Index(name="idx_token_unique", columnList="token", unique = true)
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerificationToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;                      // FK (연관관계 단순화: Long으로 보관)

    @Column(nullable=false, length=128, unique = true)
    private String token;                     // base64url or hex

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=32)
    private Purpose purpose;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="used_at")
    private LocalDateTime usedAt;

    @Column(name="sent_ip", length=64)
    private String sentIp;

    @Column(name="sent_ua", length=255)
    private String sentUa;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    public boolean isUsableAt(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public enum Purpose { EMAIL_VERIFY }
}

