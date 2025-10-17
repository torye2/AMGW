package amgw.amgw.controller;

import amgw.amgw.dto.SignupForm;
import amgw.amgw.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signup", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@Valid @ModelAttribute("signup") SignupForm form,
                               BindingResult result) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "mismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (result.hasErrors()) {
            return "signup";
        }

        userService.register(form);
        return "redirect:/signup/success";
    }

    @GetMapping("/signup/success")
    public String signupSuccess() {
        return "signup_success";
    }

    @ExceptionHandler(DisabledException.class)
    public String handleDisabledAccount() {
        return "redirect:/login?disabled";
    }
}
