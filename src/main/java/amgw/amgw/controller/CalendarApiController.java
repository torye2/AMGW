package amgw.amgw.controller;

import amgw.amgw.calendar.model.CalendarEvent;
import amgw.amgw.service.CalendarService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {
    private final CalendarService service;

    // FullCalendar: ?start=2025-10-01&end=2025-11-01 (ISO-8601, local)
    @GetMapping("/events")
    public List<Map<String,Object>> events(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(name="tz", required=false) String tz) {

        var zone = (tz!=null && !tz.isBlank()) ? ZoneId.of(tz) : ZoneId.systemDefault();
        return service.range(start, end, zone).stream().map(this::toFullCalendar).toList();
    }

    @PostMapping("/events")
    public Map<String,Object> create(@RequestBody Upsert req){
        var startUtc = toUtc(req.start, req.allDay, req.tz);
        var endUtc   = req.end != null ? toUtc(req.end, req.allDay, req.tz) : null;
        CalendarEvent e = service.create(req.title, req.location, req.description,
                startUtc, endUtc, req.allDay, req.color);
        return toFullCalendar(e);
    }

    @PatchMapping("/events/{id}")
    public Map<String,Object> update(@PathVariable Long id, @RequestBody Upsert req){
        var startUtc = req.start != null ? toUtc(req.start, req.allDay, req.tz) : null;
        var endUtc   = req.end != null ? toUtc(req.end, req.allDay, req.tz) : null;
        CalendarEvent e = service.update(id, req.title, req.location, req.description,
                startUtc, endUtc, req.allDay, req.color);
        return toFullCalendar(e);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String,Object> toFullCalendar(CalendarEvent e){
        // FullCalendar는 start/end를 “로컬 또는 Z”로 기대 → UTC ISO 문자열 반환(Z)
        return Map.of(
                "id", e.getId(),
                "title", e.getTitle(),
                "start", e.getStartUtc().toString(),
                "end", e.getEndUtc()!=null? e.getEndUtc().toString() : null,
                "allDay", e.isAllDay(),
                "extendedProps", Map.of(
                        "location", e.getLocation(),
                        "description", e.getDescription()
                ),
                "backgroundColor", e.getColor(),
                "borderColor", e.getColor()
        );
    }

    private Instant toUtc(String isoLocalOrZ, boolean allDay, String tz){
        // allDay면 YYYY-MM-DD만 올 수 있음 → 해당 타임존의 00:00 기준으로 UTC 변환
        if(allDay && isoLocalOrZ.length()<=10){
            var zone = (tz!=null && !tz.isBlank()) ? ZoneId.of(tz) : ZoneId.systemDefault();
            return LocalDate.parse(isoLocalOrZ).atStartOfDay(zone).toInstant();
        }
        // 날짜-시간 ISO가 들어오면 Instant.parse 지원(Z 필요). Z가 없으면 로컬 가정 처리해도 됨
        if(isoLocalOrZ.endsWith("Z")) return Instant.parse(isoLocalOrZ);
        var zone = (tz!=null && !tz.isBlank()) ? ZoneId.of(tz) : ZoneId.systemDefault();
        return LocalDateTime.parse(isoLocalOrZ).atZone(zone).toInstant();
    }

    @Data
    public static class Upsert {
        private String title;
        private String location;
        private String description;
        private String start;    // "2025-10-21T09:00" 또는 "2025-10-21"
        private String end;      // optional
        private boolean allDay;
        private String color;    // optional
        private String tz;       // 브라우저 타임존 (e.g. "Asia/Seoul")
    }
}