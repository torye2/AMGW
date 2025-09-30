package amgw.amgw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;

public class RoleMappingConfig {
    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        var delegate = new OidcUserService();
        return req -> {
            var user = delegate.loadUser(req);
            var mapped = new ArrayList<GrantedAuthority>(user.getAuthorities());
            var realm = user.getClaimAsMap("realm_access");
            if (realm != null && realm.get("roles") instanceof Collection<?> roles) {
                for (Object r : roles) mapped.add(new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase()));
            }
            return new DefaultOidcUser(mapped, user.getIdToken(), user.getUserInfo());
        };
    }

}
