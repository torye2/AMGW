package amgw.amgw.chat.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ChatMessageReadPK implements Serializable {
    private Long messageId;
    private Long userId;
}
