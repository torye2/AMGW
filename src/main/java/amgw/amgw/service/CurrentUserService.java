package amgw.amgw.service;

import amgw.amgw.entity.User;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepo;

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String str) {
            // 일부 인증 구현체는 그냥 username을 String으로 반환함
            username = str;
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
        }

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found in DB: " + username));

        return user.getId();
    }

    public Long resolveUserId(Principal principal) {
        if (principal == null) throw new IllegalStateException("No principal (unauthenticated)");

        String username = principal.getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username))
                .getId();
    }
}