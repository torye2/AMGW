package amgw.amgw.service;

import amgw.amgw.attendance.model.AttendanceRequest;
import amgw.amgw.repository.AttendanceRequestRepository;
import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRequestRepository repo;
    private final CurrentUserService current;

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
}