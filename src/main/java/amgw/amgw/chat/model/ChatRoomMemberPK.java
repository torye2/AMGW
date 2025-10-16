package amgw.amgw.chat.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ChatRoomMemberPK implements Serializable {
    private Long roomId;
    private Long userId;

    public ChatRoomMemberPK() {}
    public ChatRoomMemberPK(Long roomId, Long userId) {
        this.roomId = roomId;
        this.userId = userId;
    }
}
