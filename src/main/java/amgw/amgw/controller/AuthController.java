package amgw.amgw.controller;

import amgw.amgw.dto.SignupForm;
import amgw.amgw.entity.User;
import amgw.amgw.repository.UserRepository;
import amgw.amgw.service.EmailVerificationService;
import amgw.amgw.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

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
                               HttpServletRequest req,
                               BindingResult result) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "mismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (result.hasErrors()) {
            return "signup";
        }

        Optional<User> user = userRepository.findByUsername(form.getUsername());
        if (user.isPresent()) {
            userService.register(form);
            emailVerificationService.start(user.get().getId(), req.getRemoteAddr(), req.getHeader("User-Agent"));
        }

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
