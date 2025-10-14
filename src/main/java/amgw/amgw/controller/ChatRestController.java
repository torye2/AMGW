package amgw.amgw.controller;

import amgw.amgw.chat.model.ChatMessage;
import amgw.amgw.dto.ChatDto;
import amgw.amgw.repository.UserRepository;
import amgw.amgw.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chat;
    private final UserRepository userRepo;

    public record CreateGroupReq(String name, List<Long> memberIds) {}

    @PostMapping("/rooms/direct")
    public Map<String, Object> createDirect(@RequestParam Long userId) {
        var room = chat.createDirectRoom(userId);
        return Map.of("roomId", room.getId());
    }

    @PostMapping("/rooms/group")
    public Map<String, Object> createGroup(@RequestBody CreateGroupReq req) {
        log.info("createGroup req name={} memberIds={}", req.name(), req.memberIds());
        var room = chat.createGroupRoom(req.name(), req.memberIds());
        log.info("createGroup ok roomId={}", room.getId());
        return Map.of("roomId", room.getId());
    }

    @GetMapping("/rooms")
    public List<Map<String,Object>> myRooms() {
        return chat.myRooms();
    }

    /*
    @GetMapping("/rooms/{roomId}/messages")
    public Page<ChatMessage> history(@PathVariable Long roomId,
                                     @RequestParam(defaultValue="0") int page,
                                     @RequestParam(defaultValue="50") int size) {
        return chat.history(roomId, page, size);
    }
     */
    @GetMapping("/rooms/{roomId}/messages")
    public Page<ChatDto.ChatMessageRes> history(@PathVariable Long roomId,
                                                @RequestParam(defaultValue="0") int page,
                                                @RequestParam(defaultValue="50") int size) {
        Page<ChatMessage> p = chat.history(roomId, page, size);

        return p.map(m -> new ChatDto.ChatMessageRes(
                m.getId(),
                m.getRoomId(),
                m.getSenderId(),
                // senderName: name → username → email fallback
                userRepo.findById(m.getSenderId())
                        .map(u -> u.getName() != null ? u.getName()
                                : (u.getUsername() != null ? u.getUsername() : u.getEmail()))
                        .orElse("알 수 없음"),
                m.getContent(),
                m.getContentType().name(), // ★ ChatContentType → String
                m.getCreatedAt()           // ★ LocalDateTime → Instant
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        ));
    }

    @PostMapping("/rooms/{roomId}/read/{messageId}")
    public void markRead(@PathVariable Long roomId, @PathVariable Long messageId) {
        chat.markRead(roomId, messageId);
    }



    public record InviteReq(List<Long> userIds) {}

    @PostMapping("/rooms/{roomId}/invite")
    public void invite(@PathVariable Long roomId, @RequestBody InviteReq req) {
        chat.inviteMembers(roomId, req.userIds());
    }

    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public void kick(@PathVariable Long roomId, @PathVariable Long userId) {
        chat.removeMember(roomId, userId);
    }

    @PostMapping("/rooms/{roomId}/leave")
    public void leave(@PathVariable Long roomId) {
        chat.leaveRoom(roomId);
    }
}