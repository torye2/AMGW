package amgw.amgw.service;

import amgw.amgw.dto.SignupForm;
import amgw.amgw.entity.EmailVerifyStatus;
import amgw.amgw.entity.User;
import amgw.amgw.entity.UserRole;
import amgw.amgw.entity.UserStatus;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder();

    public User register(SignupForm form) {
        User user = User.builder()
                .username(form.getUsername().trim())
                .name(form.getName().trim())
                .email(form.getEmail().trim())
                .department(form.getDepartment())
                .password(passwordEncoder.encode(form.getPassword()))
                .role(UserRole.EMPLOYEE)
                .status_code(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .email_verify_status(EmailVerifyStatus.PENDING)
                .build();
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
