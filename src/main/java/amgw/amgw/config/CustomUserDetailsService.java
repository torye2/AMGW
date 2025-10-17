package amgw.amgw.config;

import amgw.amgw.entity.User;
import amgw.amgw.entity.UserStatus;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 승인되지 않은 사용자 로그인 차단
        if (user.getStatus_code() != UserStatus.ACTIVE) {
            throw new DisabledException("Account not approved yet");
        }

        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus_code())
                .roles(List.of("EMPLOYEE"))
                .build();
    }
}

