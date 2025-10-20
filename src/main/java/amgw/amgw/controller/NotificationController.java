package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.entity.Notification;
import amgw.amgw.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    // 미확인 알림 조회
    @GetMapping("/unread")
    public List<Notification> unread(@AuthenticationPrincipal CustomUserDetails me){
        return service.getUnread(me.getUserId());
    }

    // 특정 알림 읽음 처리
    @PostMapping("/read")
    public void read(@RequestParam Long notificationId){
        service.markAsRead(notificationId);
    }

    // 모든 알림 읽음 처리
    @PostMapping("/read-all")
    public void readAll(@AuthenticationPrincipal CustomUserDetails me){
        service.getUnread(me.getUserId()).forEach(n -> service.markAsRead(n.getId()));
    }
}
