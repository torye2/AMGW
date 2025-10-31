package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notice")
@Builder
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    private Long fileId;

    private Integer noticeCount;

    @Column(nullable = false)
    private String noticeTitle;

    private String noticeDetail;

    @Column(nullable = false)
    private Long userId;

    private Timestamp registrationTime;

    private Timestamp fixTime;

    private boolean important;

}
