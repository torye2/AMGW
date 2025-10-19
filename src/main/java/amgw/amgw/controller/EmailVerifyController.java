package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@RequestMapping("/verify/email")
public class EmailVerifyController {

    private final EmailVerificationService service;

    // 인증 메일 발송(사용자 자기자신)
    @PostMapping("/start")
    public String start(@AuthenticationPrincipal CustomUserDetails me,
                        HttpServletRequest req, Model model) {
        if (me == null) return "redirect:/login";
        service.start(me.getUserId(), req.getRemoteAddr(), req.getHeader("User-Agent"));
        model.addAttribute("message", "인증 메일을 전송했습니다. 30분 안에 확인해주세요.");
        return "verify/email_sent"; // 템플릿 페이지
    }

    // (선택) 관리자용: 특정 사용자에게 재발송
    @PostMapping("/start/admin")
    // @PreAuthorize("hasRole('ADMIN')")
    public String startAdmin(@RequestParam long userId,
                             HttpServletRequest req, Model model) {
        service.start(userId, req.getRemoteAddr(), req.getHeader("User-Agent"));
        model.addAttribute("message", "해당 사용자에게 인증 메일을 재전송했습니다.");
        return "admin/ok";
    }

    // 토큰 확인
    @GetMapping("/confirm")
    public String confirm(@RequestParam String token, Model model) {
        try {
            service.confirm(token);
            model.addAttribute("ok", true);
            model.addAttribute("message", "이메일 인증이 완료되었습니다.");
            return "verify/email_done";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("ok", false);
            model.addAttribute("message", "유효하지 않거나 만료된 토큰입니다. 다시 시도해주세요.");
            return "verify/email_done";
        }
    }
}

