package amgw.amgw.calendar.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "calendar_event")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long userId;

    @Column(nullable=false, length=200) private String title;
    @Column(length=200) private String location;
    @Column(length=4000) private String description;

    @Column(nullable=false) private Instant startUtc;
    @Column private Instant endUtc;
    @Column(nullable=false) private boolean allDay;

    @Column(length=16) private String color;     // 선택 (ex. "#2563eb")

    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;

    @PrePersist void preInsert(){
        var now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate void preUpdate(){
        updatedAt = Instant.now();
    }
}
