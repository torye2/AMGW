package amgw.amgw.chat.model;

import amgw.amgw.chat.model.ChatEnums.ChatMemberRole;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_member")
@IdClass(ChatRoomMemberPK.class)
@Getter @Setter
public class ChatRoomMember {
    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatMemberRole role = ChatMemberRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}
