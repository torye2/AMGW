package amgw.amgw.chat.model;

import amgw.amgw.chat.model.ChatEnums.ChatContentType;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(name="idx_cm_room_time", columnList = "room_id, created_at"),
                @Index(name="idx_cm_sender_time", columnList = "sender_id, created_at")
        })
@Getter @Setter
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;                 // gw.users.id

    @Lob
    private String content;                // TEXT 메시지

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ChatContentType contentType = ChatContentType.TEXT;

    @Lob
    @Column(name = "file_url")
    private String fileUrl;                // FILE일 때

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
