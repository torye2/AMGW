package amgw.amgw.service;

import amgw.amgw.entity.EmailVerificationToken;
import amgw.amgw.entity.EmailVerifyStatus;
import amgw.amgw.repository.EmailVerificationTokenRepo;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepo tokenRepo;
    private final UserRepository userRepo; // 기존 User JPA Repo
    private final JavaMailSender mailSender;

    // ===== 토큰 생성 =====
    private String generateSecureToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf); // 43~44자
    }

    @Transactional
    public void start(long userId, String ip, String ua) {
        var user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("메일을 전송할 유저를 찾을 수 없습니다."));

        // 이미 VERIFIED면 굳이 새 토큰 발송 안 해도 됨 (선택)
        if (user.getEmailVerifiedAt() != null) return;

        var token = generateSecureToken();
        var now = LocalDateTime.now();

        var entity = EmailVerificationToken.builder()
                .userId(userId)
                .token(token)
                .purpose(EmailVerificationToken.Purpose.EMAIL_VERIFY)
                .expiresAt(now.plusMinutes(30))
                .createdAt(now)
                .sentIp(ip)
                .sentUa(ua)
                .build();

        tokenRepo.save(entity);
        log.info("save token");
        tokenRepo.flush();

        // ✨ 커밋된 뒤에 메일 전송 (롤백과 분리)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try {
                    log.info("send email");
                    sendVerificationMail(user.getEmail(), token);
                    log.info("[EmailVerify] mail sent to {}", user.getEmail());
                } catch (Exception e) {
                    // 메일 실패는 DB 롤백과 무관. 로그만 남긴다.
                    log.error("[EmailVerify] mail send failed: {}", e.toString(), e);
                }
            }
        });
    }

    private void sendVerificationMail(String to, String token) {
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
        log.info("SMTP check host={}, port={}, user={}", impl.getHost(), impl.getPort(), impl.getUsername());

        // 실제 서비스에선 HTML 템플릿 + MimeMessageHelper 권장
        var msg = new SimpleMailMessage();
        msg.setFrom("no-reply@amgw.local");
        msg.setTo(to);
        msg.setSubject("[AMGW] 이메일 인증 안내");
        msg.setText("""
            아래 링크를 클릭하여 이메일 인증을 완료해주세요.
            (30분 내 유효)

            http://localhost:8081/verify/email/confirm?token=%s
            """.formatted(token));
        mailSender.send(msg);
    }

    @Transactional
    public void confirm(String token) {
        var now = LocalDateTime.now();
        var t = tokenRepo.findValid(token, EmailVerificationToken.Purpose.EMAIL_VERIFY, now)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다."));

        t.setUsedAt(now); // 일회용 처리
        tokenRepo.save(t);

        var user = userRepo.findById(t.getUserId()).orElseThrow();
        user.setEmail_verify_status(EmailVerifyStatus.VERIFIED);
        user.setEmailVerifiedAt(now);
        userRepo.save(user);
    }
}


