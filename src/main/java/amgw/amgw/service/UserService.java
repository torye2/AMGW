package amgw.amgw.service;

import amgw.amgw.dto.SignupForm;
import amgw.amgw.entity.User;
import amgw.amgw.entity.UserRole;
import amgw.amgw.entity.UserStatus;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder();

    public void register(SignupForm form) {
        User user = User.builder()
                .username(form.getUsername())
                .name(form.getName())
                .email(form.getEmail())
                .department(form.getDepartment())
                .password(passwordEncoder.encode(form.getPassword()))
                .role(UserRole.EMPLOYEE)
                .status_code(UserStatus.PENDING)
                .build();
        userRepository.save(user);
    }
}
