// src/main/java/amgw/amgw/controller/ChatRestController.java
package amgw.amgw.controller;

import amgw.amgw.chat.model.ChatMessage;
import amgw.amgw.repository.UserRepository;
import amgw.amgw.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chat;
    private final UserRepository userRepo; // senderName 조회용

    // ===== 요청 DTO =====
    public record CreateGroupReq(String name, List<Long> memberIds) {}
    public record InviteReq(List<Long> userIds) {}

    // ===== 응답 DTO(히스토리 전용) =====
    public record HistoryMessageRes(
            Long id,
            Long roomId,
            Long senderId,
            String senderName,     // 사용자 표시 이름
            String content,
            String contentType,    // enum -> String
            Instant createdAt      // LocalDateTime -> Instant
    ) {}

    // ===== 방 생성 =====
    /** 1:1 방 생성 (상대 userId = gw.users.id) */
    @PostMapping("/rooms/direct")
    public Map<String, Object> createDirect(@RequestParam Long userId) {
        var room = chat.createDirectRoom(userId);
        return Map.of("roomId", room.getId());
    }

    /** 그룹 방 생성 */
    @PostMapping("/rooms/group")
    public Map<String, Object> createGroup(@RequestBody CreateGroupReq req) {
        log.info("createGroup req name={} memberIds={}", req.name(), req.memberIds());
        var room = chat.createGroupRoom(req.name(), req.memberIds());
        log.info("createGroup ok roomId={}", room.getId());
        return Map.of("roomId", room.getId());
    }

    // ===== 내 방 목록 =====
    @GetMapping("/rooms")
    public List<Map<String,Object>> myRooms() {
        // service에서 roomId/name/lastMessage 등 Map으로 구성해서 반환
        return chat.myRooms();
    }

    // ===== 메시지 히스토리 =====
    @GetMapping("/rooms/{roomId}/messages")
    public Page<HistoryMessageRes> history(@PathVariable Long roomId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {

        Page<ChatMessage> p = chat.history(roomId, page, size);

        return p.map(m -> {
            // sender 표시 이름 계산: name > username > email > "알 수 없음"
            String senderName = userRepo.findById(m.getSenderId())
                    .map(u -> {
                        if (u.getName() != null && !u.getName().isBlank()) return u.getName();
                        if (u.getUsername() != null && !u.getUsername().isBlank()) return u.getUsername();
                        if (u.getEmail() != null && !u.getEmail().isBlank()) return u.getEmail();
                        return "알 수 없음";
                    })
                    .orElse("알 수 없음");

            return new HistoryMessageRes(
                    m.getId(),
                    m.getRoomId(),
                    m.getSenderId(),
                    senderName,
                    m.getContent(),
                    m.getContentType().name(),                 // enum -> String
                    m.getCreatedAt()
                            .atZone(ZoneId.systemDefault())    // LocalDateTime -> Instant
                            .toInstant()
            );
        });
    }

    // ===== 읽음 처리 (선택) =====
    @PostMapping("/rooms/{roomId}/read/{messageId}")
    public void markRead(@PathVariable Long roomId, @PathVariable Long messageId) {
        chat.markRead(roomId, messageId);
    }

    // ===== 멤버 초대/추방/퇴장 =====
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
