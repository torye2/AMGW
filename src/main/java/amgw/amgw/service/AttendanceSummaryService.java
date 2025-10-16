package amgw.amgw.service;

import amgw.amgw.attendance.model.AttendanceLog;
import amgw.amgw.repository.AttendanceLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceSummaryService {

    private final AttendanceLogRepository repo;
    private final CurrentUserService current;

    private ZoneId zone() { return ZoneId.systemDefault(); }

    public LocalDate today() { return LocalDate.now(zone()); }

    public LocalDate weekStart() {
        // 월요일 시작 주
        return today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public LocalDate weekEnd() {
        // 일요일 끝 주
        return today().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /** 오늘 출근 시각 (없으면 null) */
    public LocalTime todayCheckIn() {
        Long userId = current.currentUserId();
        var log = repo.findByUserIdAndWorkDate(userId, today()).orElse(null);
        return log != null && log.getCheckInAt() != null ? log.getCheckInAt().toLocalTime() : null;
    }

    /** 이번주 총 근무 분 (checkIn~checkOut 합, 미퇴근은 제외) */
    public int weeklyWorkedMinutes() {
        Long userId = current.currentUserId();
        List<AttendanceLog> logs = repo.findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(userId, weekStart(), weekEnd());
        int total = 0;
        for (AttendanceLog l : logs) {
            if (l.getCheckInAt() != null && l.getCheckOutAt() != null) {
                total += Duration.between(l.getCheckInAt(), l.getCheckOutAt()).toMinutesPart()
                        + Duration.between(l.getCheckInAt(), l.getCheckOutAt()).toHoursPart() * 60;
            }
        }
        return total;
    }
}
