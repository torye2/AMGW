package amgw.amgw.repository;

import amgw.amgw.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndReadFlag(Long userId, String readFlag);
}