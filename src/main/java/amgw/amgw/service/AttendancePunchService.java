package amgw.amgw.service;

import amgw.amgw.attendance.model.AttendanceLog;
import amgw.amgw.repository.AttendanceLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
public class AttendancePunchService {

    private final AttendanceLogRepository repo;
    private final CurrentUserService current;

    private ZoneId zone() { return ZoneId.systemDefault(); }

    @Transactional
    public AttendanceLog checkIn(String note) {
        Long userId = current.currentUserId();
        LocalDate today = LocalDate.now(zone());

        var existing = repo.findByUserIdAndWorkDate(userId, today).orElse(null);
        if (existing != null && existing.getCheckInAt() != null) {
            throw new IllegalStateException("이미 출근이 등록되었습니다.");
        }

        var now = LocalDateTime.now(zone());

        if (existing == null) {
            existing = AttendanceLog.builder()
                    .userId(userId)
                    .workDate(today)
                    .source(AttendanceLog.Source.WEB)
                    .note(note)
                    .checkInAt(now)
                    .build();
        } else {
            existing.setCheckInAt(now);
            if (note != null && !note.isBlank()) existing.setNote(note);
        }
        return repo.save(existing);
    }

    @Transactional
    public AttendanceLog checkOut(String note) {
        Long userId = current.currentUserId();
        LocalDate today = LocalDate.now(zone());

        var log = repo.findByUserIdAndWorkDate(userId, today)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다. 먼저 출근을 등록하세요."));

        if (log.getCheckOutAt() != null) {
            throw new IllegalStateException("이미 퇴근이 등록되었습니다.");
        }

        var now = LocalDateTime.now(zone());
        log.setCheckOutAt(now);
        if (note != null && !note.isBlank()) log.setNote(note);
        return repo.save(log);
    }

    @Transactional(readOnly = true)
    public AttendanceLog today() {
        Long userId = current.currentUserId();
        return repo.findByUserIdAndWorkDate(userId, LocalDate.now(zone())).orElse(null);
    }
}