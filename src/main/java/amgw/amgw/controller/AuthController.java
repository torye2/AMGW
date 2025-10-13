package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    String login() { return "redirect:/oauth2/authorization/keycloak"; }
}
