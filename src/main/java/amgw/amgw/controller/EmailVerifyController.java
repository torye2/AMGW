package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@RequestMapping("/verify/email")
@Slf4j
public class EmailVerifyController {

    private final EmailVerificationService service;

    // 인증 메일 발송(사용자 자기자신)
    @PostMapping("/start")
    public String start(@AuthenticationPrincipal CustomUserDetails me,
                        HttpServletRequest req, Model model) {
        if (me == null) return "redirect:/login";
        service.start(me.getUserId(), req.getRemoteAddr(), req.getHeader("User-Agent"));
        model.addAttribute("message", "인증 메일을 전송했습니다. 30분 안에 확인해주세요.");
        return "email_sent"; // 템플릿 페이지
    }

    @PostMapping("/verify/email/start-guest")
    public String startGuest(HttpServletRequest req, Model model) {
        Long uid = (Long) req.getSession().getAttribute("pendingVerifyUserId");
        if (uid == null) {
            // 세션 만료/새 탭 등
            model.addAttribute("message", "세션이 만료되었습니다. 로그인 후 재전송해주세요.");
            return "email_sent";
        }
        service.start(uid, req.getRemoteAddr(), req.getHeader("User-Agent"));
        model.addAttribute("message", "인증 메일을 다시 전송했습니다. 30분 안에 확인해주세요.");
        return "email_sent";
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
            log.info("confirm email verification");
            service.confirm(token);
            model.addAttribute("ok", true);
            model.addAttribute("message", "이메일 인증이 완료되었습니다.");
            log.info("confirm email verification success");
            return "email_done";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("ok", false);
            model.addAttribute("message", "유효하지 않거나 만료된 토큰입니다. 다시 시도해주세요.");
            return "email_done";
        }
    }
}

