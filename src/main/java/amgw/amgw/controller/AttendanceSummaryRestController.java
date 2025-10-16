package amgw.amgw.controller;

import amgw.amgw.service.AttendanceSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceSummaryRestController {

    private final AttendanceSummaryService svc;

    /**
     * 응답 예시:
     * { "todayCheckIn": "09:12", "weeklyHoursText": "32h 50m" }
     */
    @GetMapping("/summary")
    public Map<String, String> summary() {
        LocalTime in = svc.todayCheckIn();
        String todayCheckIn = (in == null) ? "--:--" : String.format("%02d:%02d", in.getHour(), in.getMinute());

        int mins = svc.weeklyWorkedMinutes();
        int h = mins / 60;
        int m = mins % 60;
        String weeklyHoursText = (mins == 0) ? "0h 0m" : (h + "h " + m + "m");

        return Map.of(
                "todayCheckIn", todayCheckIn,
                "weeklyHoursText", weeklyHoursText
        );
    }
}
