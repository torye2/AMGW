package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String type;
    private String summary;

    @Lob
    private String data;

    @Column(name = "read_flag", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String readFlag;

    private LocalDateTime createdAt = LocalDateTime.now();
}
