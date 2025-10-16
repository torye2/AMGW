package amgw.amgw.chat.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_read",
        indexes = { @Index(name="idx_cmr_user", columnList = "user_id") })
@IdClass(ChatMessageReadPK.class)
@Getter @Setter
public class ChatMessageRead {
    @Id
    @Column(name = "message_id")
    private Long messageId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt = LocalDateTime.now();
}
