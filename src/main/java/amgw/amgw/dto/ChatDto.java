package amgw.amgw.dto;

import java.time.Instant;

public class ChatDto {
    public record SendMessageReq(Long roomId, String content, String contentType) {}
    public record ChatMessageRes(Long id, Long roomId, Long senderId, String senderName, String content, String contentType, Instant createdAt) {}
}
