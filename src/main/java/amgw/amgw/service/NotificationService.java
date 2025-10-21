package amgw.amgw.service;

import amgw.amgw.entity.Notification;
import amgw.amgw.repository.NotificationRepository;
import amgw.amgw.repository.UserRepository;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final SimpMessagingTemplate messaging;
    private final UserRepository userRepo; // id -> username 조회

    /** 알림 생성 + DB 저장 + STOMP 전송 (payload에 extra 포함) */
    public void pushNotification(Long userId, String type, String summary, Map<String, Object> extraData) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSummary(summary);
        n.setData(extraData != null ? new Gson().toJson(extraData) : null);
        n.setReadFlag("N");
        repo.save(n);

        String username = userRepo.findUsernameById(userId);
        if (username != null) {
            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("id", n.getId());
            payload.put("type", n.getType());
            payload.put("summary", n.getSummary());
            if (extraData != null) payload.putAll(extraData);

            messaging.convertAndSendToUser(username, "/queue/notifications", payload);
        }
    }

    @Transactional
    public void markAsRead(Long notificationId){
        Notification n = repo.findById(notificationId).orElseThrow();
        n.setReadFlag("Y");
    }

    public List<Notification> getUnread(Long userId){
        return repo.findByUserIdAndReadFlag(userId, "N");
    }

    /** 같은 채팅방(roomId)의 미확인 알림을 한 번에 읽음 처리 */
    @Transactional
    public void markAllByRoom(Long userId, Long roomId){
        List<Notification> unread = repo.findByUserIdAndReadFlag(userId, "N");
        for (Notification n : unread) {
            String json = n.getData();
            if (json == null || json.isBlank()) continue;

            try {
                var obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("roomId")) {
                    long rid = obj.get("roomId").getAsLong(); // <- Double 문제 없이 Long으로 안전 추출
                    if (rid == roomId) {
                        n.setReadFlag("Y"); // 트랜잭션 커밋 시 UPDATE 반영
                    }
                }
            } catch (Exception ignored) { }
        }
    }
}
