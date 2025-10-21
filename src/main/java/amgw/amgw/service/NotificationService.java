package amgw.amgw.service;

import amgw.amgw.entity.Notification;
import amgw.amgw.repository.NotificationRepository;
import amgw.amgw.repository.UserRepository;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final SimpMessagingTemplate messaging;
    private final UserRepository userRepo;

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
            // ✅ summary + type + extraData 모두 전송
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("id", n.getId());
            payload.put("type", n.getType());
            payload.put("summary", n.getSummary());
            if (extraData != null) payload.putAll(extraData);

            messaging.convertAndSendToUser(username, "/queue/notifications", payload);
        }
    }



    /** 단건 읽음 처리 */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification n = repo.findById(notificationId).orElseThrow();
        n.setReadFlag("Y");
    }

    /** 미확인 목록 */
    public List<Notification> getUnread(Long userId) {
        return repo.findByUserIdAndReadFlag(userId, "N");
    }
}
