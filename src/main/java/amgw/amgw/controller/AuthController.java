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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
                               BindingResult result,
                               Model model) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "mismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (result.hasErrors()) {
            return "signup";
        }

        if (userService.existsByUsername(form.getUsername())) {
            result.rejectValue("username", "dup", "이미 사용 중인 아이디입니다.");
            return "signup";
        }

        User user = userService.register(form);

        try {
            emailVerificationService.start(
                    user.getId(),
                    req.getRemoteAddr(),
                    req.getHeader("User-Agent")
            );
            model.addAttribute("message", "회원가입 완료! 이메일을 확인해 인증을 완료해주세요.");
        } catch (Exception e) {
            // 메일 실패해도 가입은 성공했음을 명확히 안내
            model.addAttribute("message", "가입은 완료되었으나 메일 발송에 실패했습니다. 재전송 버튼으로 다시 시도하세요.");
        }
        return "email_sent";
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
