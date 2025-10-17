package amgw.amgw.config;

import amgw.amgw.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails, Serializable {
    private final Long userId;          // PK
    private final String username;      // 로그인 ID
    private final String password;      // 해시
    private final String name;          // 실명/표시명
    private final String email;
    private final UserStatus status;    // PENDING/ACTIVE/INACTIVE/SUSPENDED/TERMINATED
    private final List<String> roles;   // ["ROLE_EMPLOYEE", "ROLE_ADMIN"...]

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public String toString() { // 비밀번호 유출 방지
        return "CustomUserDetails(userId=%d, username=%s, name=%s, email=%s, status=%s)"
                .formatted(userId, username, name, email, status);
    }
}
