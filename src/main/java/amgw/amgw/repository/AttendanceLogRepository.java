package amgw.amgw.repository;

import amgw.amgw.attendance.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    Optional<AttendanceLog> findByUserIdAndWorkDate(Long userId, LocalDate workDate);
    Optional<AttendanceLog> findByUserIdAndWorkDateAndCheckOutAtIsNull(Long userId, LocalDate workDate);

    // 주간 합계
    List<AttendanceLog> findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(Long userId, LocalDate start, LocalDate end);
}