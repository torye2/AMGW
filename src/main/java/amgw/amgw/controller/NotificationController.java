package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.entity.Notification;
import amgw.amgw.service.CurrentUserService;
import amgw.amgw.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;
    private final CurrentUserService current;

    /** 미확인 목록 (세션 사용자) */
    @GetMapping("/unread")
    public List<Map<String,Object>> unread(){
        Long me = current.currentUserId();
        return service.getUnread(me).stream().map(n -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", n.getId());
            m.put("type", n.getType());
            m.put("summary", n.getSummary());
            m.put("data", n.getData());           // 클라이언트에서 JSON.parse()
            m.put("createdAt", n.getCreatedAt()); // 표시용
            return m;
        }).toList();
    }

    /** 단건 읽음 */
    @PostMapping("/read-one")
    public void readOne(@RequestParam("id") Long id){
        service.markAsRead(id);
    }

    /** 같은 채팅방 알림 묶음 읽음 */
    @PostMapping("/read-by-room")
    public void readByRoom(@RequestParam("roomId") Long roomId){
        Long me = current.currentUserId();
        service.markAllByRoom(me, roomId);
    }
}

