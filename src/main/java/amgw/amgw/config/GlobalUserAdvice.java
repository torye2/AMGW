package amgw.amgw.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserAdvice {

    @ModelAttribute("me")
    public CustomUserDetails addUserToModel(@AuthenticationPrincipal CustomUserDetails user) {
        return user;
    }
}
