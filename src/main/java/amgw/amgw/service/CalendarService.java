package amgw.amgw.service;

import amgw.amgw.calendar.model.CalendarEvent;
import amgw.amgw.repository.CalendarEventRepository;
import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
public class CalendarService {
    private final CalendarEventRepository repo;
    private final CurrentUserService current;

    @Transactional(readOnly = true)
    public List<CalendarEvent> range(LocalDate start, LocalDate end, ZoneId zone) {
        Long uid = current.currentUserId();
        // FullCalendar는 [start, end)로 넘겨오므로 end 하루 빼서 포함되게 처리
        var startUtc = start.atStartOfDay(zone).toInstant();
        var endUtcExcl = end.atStartOfDay(zone).minusNanos(1).toInstant();

        List<CalendarEvent> a = repo.findByUserIdAndStartUtcLessThanEqualAndEndUtcGreaterThanEqualOrderByStartUtcAsc(
                uid, endUtcExcl, startUtc);
        List<CalendarEvent> b = repo.findByUserIdAndEndUtcIsNullAndStartUtcBetweenOrderByStartUtcAsc(
                uid, startUtc, endUtcExcl);

        var out = new ArrayList<CalendarEvent>(a);
        out.addAll(b);
        return out;
    }

    @Transactional
    public CalendarEvent create(String title, String location, String description,
                                Instant startUtc, Instant endUtc, boolean allDay, String color) {
        var e = CalendarEvent.builder()
                .userId(current.currentUserId())
                .title(title)
                .location(location)
                .description(description)
                .startUtc(startUtc)
                .endUtc(endUtc)
                .allDay(allDay)
                .color(color)
                .build();
        return repo.save(e);
    }

    @Transactional
    public CalendarEvent update(Long id, String title, String location, String description,
                                Instant startUtc, Instant endUtc, boolean allDay, String color){
        var e = repo.findById(id).orElseThrow();
        if(!e.getUserId().equals(current.currentUserId())) throw new IllegalStateException("권한 없음");
        if(title!=null) e.setTitle(title);
        if(location!=null) e.setLocation(location);
        if(description!=null) e.setDescription(description);
        if(startUtc!=null) e.setStartUtc(startUtc);
        // endUtc는 null 허용(하루짜리)
        e.setEndUtc(endUtc);
        e.setAllDay(allDay);
        if(color!=null) e.setColor(color);
        return e;
    }

    @Transactional
    public void delete(Long id){
        var e = repo.findById(id).orElseThrow();
        if(!e.getUserId().equals(current.currentUserId())) throw new IllegalStateException("권한 없음");
        repo.delete(e);
    }
}