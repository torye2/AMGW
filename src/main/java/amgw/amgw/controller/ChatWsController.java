package amgw.amgw.controller;

import amgw.amgw.chat.model.ChatEnums.ChatContentType;
import amgw.amgw.chat.model.ChatMessage;
import amgw.amgw.repository.UserRepository;
import amgw.amgw.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate broker;
    private final UserRepository userRepo;

    public static record SendReq(Long roomId, String content, String contentType) {}

    // senderName & createdAt(Instant) 포함해서 브로드캐스트
    public static record MsgRes(
            Long id,
            Long roomId,
            Long senderId,
            String senderName,
            String content,
            String contentType,
            Instant createdAt
    ) {}

    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable Long roomId, SendReq req, Principal principal) {

        // contentType 안전 파싱 (null/빈문자/소문자 대응)
        ChatContentType type = Optional.ofNullable(req.contentType())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(ChatContentType::valueOf)
                .orElse(ChatContentType.TEXT);

        // 저장 (principal에서 보낸 사람 id 해석은 chatService.postMessage 내부)
        ChatMessage saved = chatService.postMessage(principal, roomId, req.content(), type);

        // 보낸 사람 이름 조회 (name → username → email fallback)
        String senderName = userRepo.findById(saved.getSenderId())
                .map(u -> u.getName() != null ? u.getName()
                        : (u.getUsername() != null ? u.getUsername() : u.getEmail()))
                .orElse("알 수 없음");

        // LocalDateTime → Instant (프론트 일관성)
        Instant createdAt = saved.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        MsgRes res = new MsgRes(
                saved.getId(),
                saved.getRoomId(),
                saved.getSenderId(),
                senderName,
                saved.getContent(),
                saved.getContentType().name(),        // enum → String
                createdAt
        );

        broker.convertAndSend("/topic/rooms/" + roomId, res);
    }
}
