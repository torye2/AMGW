package amgw.amgw.controller;

import amgw.amgw.attendance.model.AttendanceLog;
import amgw.amgw.service.AttendancePunchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendancePunchRestController {

    private final AttendancePunchService service;

    public record PunchReq(String note) {}
    public record PunchRes(Long id, String workDate, String checkInAt, String checkOutAt) {}

    private PunchRes toRes(AttendanceLog l) {
        var z = ZoneId.systemDefault();
        return new PunchRes(
                l.getId(),
                l.getWorkDate() != null ? l.getWorkDate().toString() : null,
                l.getCheckInAt() != null ? l.getCheckInAt().atZone(z).toInstant().toString() : null,
                l.getCheckOutAt() != null ? l.getCheckOutAt().atZone(z).toInstant().toString() : null
        );
    }

    @PostMapping("/check-in")
    public PunchRes checkIn(@RequestBody(required = false) PunchReq req) {
        var saved = service.checkIn(req != null ? req.note() : null);
        return toRes(saved);
    }

    @PostMapping("/check-out")
    public PunchRes checkOut(@RequestBody(required = false) PunchReq req) {
        var saved = service.checkOut(req != null ? req.note() : null);
        return toRes(saved);
    }

    @GetMapping("/today")
    public PunchRes today() {
        var l = service.today();
        return l == null ? null : toRes(l);
    }

    // 에러 메시지 프런트로 전달
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> handleIllegal(IllegalStateException e){
        return Map.of("message", e.getMessage());
    }
}
