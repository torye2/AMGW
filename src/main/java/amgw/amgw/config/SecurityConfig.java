package amgw.amgw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clients;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var oidcLogout = new OidcClientInitiatedLogoutSuccessHandler(clients);
        oidcLogout.setPostLogoutRedirectUri("{baseUrl}/");

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/error", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**",
                                "/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler((req, res, auth) -> {
                            // OAuth2User user = (OAuth2User) authn.getPrincipal();
                            // String sub = user.getAttribute("sub"); String email = user.getAttribute("email"); ...
                            // userService.provisionIfNeeded(sub, email, ...);
                            res.sendRedirect("/");
                        })
                )
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogout)
                );
        return http.build();
    }
}
