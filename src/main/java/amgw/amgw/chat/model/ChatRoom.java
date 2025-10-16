package amgw.amgw.chat.model;

import amgw.amgw.chat.model.ChatEnums.ChatRoomType;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room") // 테이블: gw.chat_room
@Getter @Setter
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomType type;         // DIRECT/GROUP

    @Column(length = 191)
    private String name;               // GROUP일 때 방 이름

    @Column(name = "created_by", nullable = false)
    private Long createdBy;            // gw.users.id

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
