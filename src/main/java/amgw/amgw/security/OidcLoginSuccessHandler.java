package amgw.amgw.security;

import amgw.amgw.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OidcLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("✅ OIDC success for principal={}", authentication.getName());

        var principal = authentication.getPrincipal();
        if (principal instanceof DefaultOidcUser oidc) {
            var claims = oidc.getClaims();
            String provider = "keycloak";
            String sub      = String.valueOf(claims.get("sub"));
            String username = (String) claims.getOrDefault("preferred_username", null);
            String email    = (String) claims.getOrDefault("email", null);
            String name     = (String) claims.getOrDefault("name", null);
            String picture  = (String) claims.getOrDefault("picture", null);

            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            List<String> realmRoles = realmAccess == null ? List.of() : (List<String>) realmAccess.getOrDefault("roles", List.of());
            String rolesJson = om.writeValueAsString(Map.of("realm", realmRoles));

            var user = userRepository.findByProviderAndSubject(provider, sub)
                    .map(u -> {
                        u.setUsername(username);
                        u.setEmail(email);
                        u.setName(name);
                        u.setPicture(picture);
                        u.setRolesJson(rolesJson);
                        u.setLastLoginAt(LocalDateTime.now());
                        return u;
                    })
                    .orElseGet(() -> {
                        var u = new UserEntity();
                        u.setProvider(provider);
                        u.setSubject(sub);
                        u.setUsername(username);
                        u.setEmail(email);
                        u.setName(name);
                        u.setPicture(picture);
                        u.setRolesJson(rolesJson);
                        u.setLastLoginAt(LocalDateTime.now());
                        return u;
                    });

            userRepository.save(user);
            log.info("💾 upserted user provider={} sub={}", provider, sub);
        }

        super.onAuthenticationSuccess(request, response, authentication); // 3개 인자!
    }
}