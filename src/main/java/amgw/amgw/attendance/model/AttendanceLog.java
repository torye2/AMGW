package amgw.amgw.attendance.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_log",
        uniqueConstraints = @UniqueConstraint(name = "uk_attlog_user_date", columnNames = {"user_id","work_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceLog {

    public enum Source { WEB, MOBILE, ADMIN, AUTO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="work_date", nullable=false)
    private LocalDate workDate;

    @Column(name="check_in_at")
    private LocalDateTime checkInAt;

    @Column(name="check_out_at")
    private LocalDateTime checkOutAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Source source = Source.WEB;

    private String note;

    @Column(name = "created_at", nullable=false)
    private java.sql.Timestamp createdAt;

    @Column(name = "updated_at", nullable=false)
    private java.sql.Timestamp updatedAt;

    @PrePersist
    public void prePersist() {
        var now = java.time.Instant.now();
        if (createdAt == null) createdAt = java.sql.Timestamp.from(now);
        if (updatedAt == null) updatedAt = java.sql.Timestamp.from(now);
    }
    @PreUpdate
    public void preUpdate() {
        updatedAt = java.sql.Timestamp.from(java.time.Instant.now());
    }
}
