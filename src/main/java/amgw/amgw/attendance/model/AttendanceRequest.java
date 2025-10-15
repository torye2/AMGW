package amgw.amgw.attendance.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    public enum Type { VACATION, SICK, WFH, OUT }
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    // ★ 기본값을 엔티티에서 보장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String reason;

    private Long approverId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (status == null) status = Status.PENDING;
        Instant now = Instant.now();
        createdAt = (createdAt == null ? now : createdAt);
        updatedAt = (updatedAt == null ? now : updatedAt);
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}