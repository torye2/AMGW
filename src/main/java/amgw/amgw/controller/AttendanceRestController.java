package amgw.amgw.controller;

import amgw.amgw.attendance.model.AttendanceRequest;
import amgw.amgw.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceRestController {
    private final AttendanceService service;

    // 생성
    @PostMapping("/requests")
    public Map<String,Object> create(
            @RequestParam AttendanceRequest.Type type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false, defaultValue = "") String reason
    ){
        var saved = service.create(type, startDate, endDate, startTime, endTime, reason);
        return Map.of("ok", true, "id", saved.getId());
    }

    // 내 목록
    @GetMapping("/requests/my")
    public List<AttendanceRequest> my(){
        return service.myRequests();
    }

    // (선택) 승인/반려 – 관리자나 승인권자만 사용
    @PostMapping("/requests/{id}/approve")
    public Map<String,Object> approve(@PathVariable Long id){
        var saved = service.approve(id, null);
        return Map.of("ok", true, "status", saved.getStatus());
    }

    @PostMapping("/requests/{id}/reject")
    public Map<String,Object> reject(@PathVariable Long id){
        var saved = service.reject(id, null);
        return Map.of("ok", true, "status", saved.getStatus());
    }
}