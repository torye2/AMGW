package amgw.amgw.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            String loginId = user.getUsername();
            model.addAttribute("loginId", loginId);
            model.addAttribute("username", user.getUsername());
            return "index";
        } else {
            return "index";
        }
    }
}


