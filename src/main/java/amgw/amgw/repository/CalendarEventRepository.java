package amgw.amgw.repository;

import amgw.amgw.calendar.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserIdAndStartUtcLessThanEqualAndEndUtcGreaterThanEqualOrderByStartUtcAsc(
            Long userId, Instant end, Instant start);

    List<CalendarEvent> findByUserIdAndEndUtcIsNullAndStartUtcBetweenOrderByStartUtcAsc(
            Long userId, Instant start, Instant end);
}