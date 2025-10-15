package amgw.amgw.repository;

import amgw.amgw.attendance.model.AttendanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRequestRepository extends JpaRepository<AttendanceRequest, Long> {
    List<AttendanceRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}