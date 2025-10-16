package amgw.amgw.service;

import amgw.amgw.attendance.model.AttendanceRequest;
import amgw.amgw.repository.AttendanceRequestRepository;
import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRequestRepository repo;
    private final CurrentUserService current;
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public AttendanceRequest create(AttendanceRequest.Type type,
                                    LocalDate startDate, LocalDate endDate,
                                    LocalTime startTime, LocalTime endTime,
                                    String reason) {
        var req = AttendanceRequest.builder()
                .userId(current.currentUserId())
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason)
                .build();
        return repo.save(req);
    }

    public List<AttendanceRequest> myRequests(){
        return repo.findByUserIdOrderByCreatedAtDesc(current.currentUserId());
    }

    public AttendanceRequest approve(Long id, Long approverId){
        var r = repo.findById(id).orElseThrow();
        r.setStatus(AttendanceRequest.Status.APPROVED);
        r.setApproverId(approverId);
        return repo.save(r);
    }

    public AttendanceRequest reject(Long id, Long approverId){
        var r = repo.findById(id).orElseThrow();
        r.setStatus(AttendanceRequest.Status.REJECTED);
        r.setApproverId(approverId);
        return repo.save(r);
    }

    /**
     * 오늘의 첫 출근 시간 "HH:mm" 또는 데이터 없으면 null
     */
    public String findTodayCheckInTimeOrNull() {
        // TODO: 출근 로그에서 오늘 첫 check-in 시각 조회 로직으로 교체
        // 예시) AttendanceLogRepository.findFirstByUserIdAndDateOrderByTimeAsc(...)
        //       있으면 return time.format(HM);
        return null;
    }

    /**
     * 이번 주(월~일) 누적 근무시간을 "Xh Ym" 형식으로 반환. 데이터 없으면 null
     */
    public String calcWeeklyHoursTextOrNull() {
        // TODO: 주간 근무 기록 합산 로직으로 교체
        // 예시) Duration total = repository.sumThisWeek(...);
        //       return "%dh %dm".formatted(total.toHours(), total.toMinutesPart());
        return null;
    }

    /**
     * 남은 연차(일). 없으면 null
     */
    public Double findVacationLeftOrNull() {
        // TODO: 연차 계정/정책 테이블에서 조회
        return null;
    }

    /**
     * 현재 상태 문자열(예: "근무중", "퇴근", "휴가", "외근" 등). 없으면 null
     */
    public String findCurrentStatusOrNull() {
        // TODO: 오늘의 체크인/체크아웃/휴가 신청 상태 등으로 판정
        return null;
    }
}