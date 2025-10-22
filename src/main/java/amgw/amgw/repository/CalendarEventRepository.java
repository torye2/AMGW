package amgw.amgw.repository;

import amgw.amgw.calendar.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByUserIdAndStartUtcLessThanEqualAndEndUtcGreaterThanEqualOrderByStartUtcAsc(
            Long userId, Instant end, Instant start);

    List<CalendarEvent> findByUserIdAndEndUtcIsNullAndStartUtcBetweenOrderByStartUtcAsc(
            Long userId, Instant start, Instant end);

    @Query("""
        select e from CalendarEvent e
         where e.userId = :uid
           and e.startUtc < :to
           and coalesce(e.endUtc, e.startUtc) >= :from
         order by e.startUtc
        """)
    List<CalendarEvent> findRange(
            @Param("uid") Long uid,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
