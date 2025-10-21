package amgw.amgw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Controller
@RequiredArgsConstructor
public class CalendarPageController {

    @GetMapping("/calendar")
    public String calendar(Model model,
                           @AuthenticationPrincipal OidcUser oidc) {
        // 템플릿에서 th:if="${me != null}" / ${me.name} 그대로 사용 가능
        if (oidc != null) {
            model.addAttribute("me", new Me(oidc.getFullName()));
        } else {
            model.addAttribute("me", null);
        }
        return "calendar";
    }

    // 템플릿에서 쓰기 위한 최소 DTO
    record Me(String name) {}
}