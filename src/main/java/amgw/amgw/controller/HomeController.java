package amgw.amgw.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User user) {
        if (user != null) {
            String loginId = user.getAttribute("preferred_username");
            model.addAttribute("loginId", loginId);
            model.addAttribute("username", user.getAttribute("name"));
            model.addAttribute("email", user.getAttribute("email"));
            return "index";
        } else {
            return "/login";
        }
    }
}

