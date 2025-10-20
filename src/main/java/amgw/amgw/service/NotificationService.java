package amgw.amgw.service;

import amgw.amgw.entity.Notification;
import amgw.amgw.repository.NotificationRepository;
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

    // 알림 생성 + 실시간 전송
    public void pushNotification(Long userId, String type, String summary, Object extraData){
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSummary(summary);
        n.setData(extraData != null ? new Gson().toJson(extraData) : null);
        repo.save(n);

        // STOMP 실시간 전송
        messaging.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                Map.of(
                        "id", n.getId(),
                        "type", n.getType(),
                        "summary", n.getSummary()
                )
        );
    }

    // 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId){
        Notification n = repo.findById(notificationId).orElseThrow();
        n.setReadFlag("Y");
    }

    // 미확인 알림 조회
    public List<Notification> getUnread(Long userId){
        return repo.findByUserIdAndReadFlag(userId, "N");
    }
}
