package amgw.amgw.todo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="todo")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Todo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long userId;
    @Column(nullable=false) private String title;

    @Column(nullable=false) private boolean done;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private Integer sortOrder;

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp   private LocalDateTime updatedAt;

    public enum Priority { LOW, NORMAL, HIGH }
}
