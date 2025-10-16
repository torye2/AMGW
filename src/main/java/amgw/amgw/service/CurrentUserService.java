package amgw.amgw.service;

import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepo;

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof DefaultOidcUser oidc)) {
            throw new IllegalStateException("Unauthenticated or not OIDC");
        }
        String sub = oidc.getSubject(); // == claims.get("sub")
        return userRepo.findByProviderAndSubject("keycloak", sub)
                .orElseThrow(() -> new IllegalStateException("user not provisioned"))
                .getId();
    }

    public Long resolveUserId(Principal principal) {
        if (principal instanceof org.springframework.security.core.Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof DefaultOidcUser oidc) {
                String sub = oidc.getSubject();
                return userRepo.findByProviderAndSubject("keycloak", sub)
                        .orElseThrow(() -> new IllegalStateException("user not provisioned")).getId();
            }
        }
        throw new IllegalStateException("Unauthenticated (WS)");
    }
}