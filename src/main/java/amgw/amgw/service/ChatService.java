package amgw.amgw.service;

import amgw.amgw.chat.model.*;
import amgw.amgw.chat.model.ChatEnums.ChatContentType;
import amgw.amgw.chat.model.ChatEnums.ChatMemberRole;
import amgw.amgw.repository.ChatRoomRepository;
import amgw.amgw.service.CurrentUserService;
import amgw.amgw.repository.ChatMessageReadRepository;
import amgw.amgw.repository.ChatMessageRepository;
import amgw.amgw.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatMessageReadRepository readRepo;
    private final CurrentUserService current;

    /* 방 만들기: 1:1(DIRECT) */
    @Transactional
    public ChatRoom createDirectRoom(Long otherUserId) {
        Long me = current.currentUserId();

        // 이미 존재하는 DIRECT 방 재사용 (간단 버전: 조인 테이블로 2명인 방 검색)
        List<ChatRoomMember> mine = memberRepo.findByUserId(me);
        for (ChatRoomMember m : mine) {
            // 해당 방의 멤버가 2명이고 otherUserId 포함이면 재사용
            long count = memberRepo.findByUserId(otherUserId).stream()
                    .filter(x -> Objects.equals(x.getRoomId(), m.getRoomId())).count();
            if (count > 0) return roomRepo.findById(m.getRoomId()).orElseThrow();
        }

        ChatRoom room = new ChatRoom();
        room.setType(ChatEnums.ChatRoomType.DIRECT);
        room.setCreatedBy(me);
        room.setCreatedAt(LocalDateTime.now());
        room = roomRepo.save(room);

        ChatRoomMember a = new ChatRoomMember();
        a.setRoomId(room.getId()); a.setUserId(me); a.setRole(ChatMemberRole.MEMBER);
        ChatRoomMember b = new ChatRoomMember();
        b.setRoomId(room.getId()); b.setUserId(otherUserId); b.setRole(ChatMemberRole.MEMBER);
        memberRepo.saveAll(List.of(a, b));
        return room;
    }

    /* 방 만들기: 그룹 */
    /*
    @Transactional
    public ChatRoom createGroupRoom(String name, List<Long> memberIds) {
        Long me = current.currentUserId();
        ChatRoom room = new ChatRoom();
        room.setType(ChatEnums.ChatRoomType.GROUP);
        room.setName(name);
        room.setCreatedBy(me);
        room.setCreatedAt(LocalDateTime.now());
        room = roomRepo.save(room);

        List<ChatRoomMember> joins = new ArrayList<>();
        // 방장 + 멤버들
        Set<Long> unique = new HashSet<>(memberIds);
        unique.add(me);
        for (Long uid : unique) {
            ChatRoomMember m = new ChatRoomMember();
            m.setRoomId(room.getId());
            m.setUserId(uid);
            m.setRole(uid.equals(me) ? ChatMemberRole.OWNER : ChatMemberRole.MEMBER);
            joins.add(m);
        }
        memberRepo.saveAll(joins);
        return room;
    }
     */
    @Transactional
    public ChatRoom createGroupRoom(String name, List<Long> memberIds) {
        Long me = current.currentUserId();
        ChatRoom room = new ChatRoom();
        room.setType(ChatEnums.ChatRoomType.GROUP);
        room.setName(name);
        room.setCreatedBy(me);
        room.setCreatedAt(LocalDateTime.now());
        room = roomRepo.save(room);

        // 중복 제거 + 나 자신 추가
        Set<Long> all = new LinkedHashSet<>(memberIds == null ? List.of() : memberIds);
        all.add(me);

        List<ChatRoomMember> joins = new ArrayList<>();
        for (Long uid : all) {
            ChatRoomMember m = new ChatRoomMember();
            m.setRoomId(room.getId());
            m.setUserId(uid);
            m.setRole(uid.equals(me) ? ChatMemberRole.OWNER : ChatMemberRole.MEMBER);
            joins.add(m);
        }
        memberRepo.saveAll(joins);
        return room;
    }

    @Transactional
    public ChatMessage postMessage(Principal principal, Long roomId, String content, ChatContentType type) {
        Long me = current.resolveUserId(principal); // ★ principal 기반
        if (!memberRepo.existsByRoomIdAndUserId(roomId, me)) throw new SecurityException("not a member");

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setSenderId(me);
        msg.setContent(content);
        msg.setContentType(type == null ? ChatContentType.TEXT : type);
        msg.setCreatedAt(LocalDateTime.now());
        msg = messageRepo.save(msg);

        ChatMessageRead read = new ChatMessageRead();
        read.setMessageId(msg.getId());
        read.setUserId(me);
        read.setReadAt(LocalDateTime.now());
        readRepo.save(read);

        return msg;
    }

    /* 내 방 목록 (아주 간단 버전) */
    @Transactional(readOnly = true)
    public List<Map<String,Object>> myRooms() {
        Long me = current.currentUserId();
        List<ChatRoomMember> joins = memberRepo.findByUserId(me);
        List<Map<String,Object>> out = new ArrayList<>();
        for (ChatRoomMember j : joins) {
            var room = roomRepo.findById(j.getRoomId()).orElseThrow();
            // 최근 메시지 1건
            Page<ChatMessage> p = messageRepo.findByRoomIdOrderByCreatedAtDesc(room.getId(), PageRequest.of(0,1));
            ChatMessage last = p.isEmpty()? null : p.getContent().get(0);
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("roomId", room.getId());
            map.put("type", room.getType().name());
            map.put("name", room.getName());
            map.put("lastMessage", last != null ? last.getContent() : null);
            out.add(map);
        }
        return out;
    }

    /* 히스토리 페이지네이션(최신→과거 정렬) */
    @Transactional(readOnly = true)
    public Page<ChatMessage> history(Long roomId, int page, int size) {
        Long me = current.currentUserId();
        if (!memberRepo.existsByRoomIdAndUserId(roomId, me))
            throw new SecurityException("not a member");
        return messageRepo.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(page, size));
    }

    /* 읽음 표시 */
    @Transactional
    public void markRead(Long roomId, Long messageId) {
        Long me = current.currentUserId();
        if (!memberRepo.existsByRoomIdAndUserId(roomId, me))
            throw new SecurityException("not a member");
        ChatMessageRead key = new ChatMessageRead();
        key.setMessageId(messageId);
        key.setUserId(me);
        key.setReadAt(LocalDateTime.now());
        readRepo.save(key);
    }

    @Transactional
    public void inviteMembers(Long roomId, List<Long> userIds) {
        Long me = current.currentUserId();
        // 권한: OWNER만 초대 가능
        var my = memberRepo.findById(new ChatRoomMemberPK(){{
            setRoomId(roomId); setUserId(me);
        }}).orElseThrow(() -> new SecurityException("not a member"));
        if (my.getRole() != ChatMemberRole.OWNER) throw new SecurityException("owner only");

        // 중복/기존 멤버 제외 후 insert
        for (Long uid : new LinkedHashSet<>(userIds)) {
            if (memberRepo.existsByRoomIdAndUserId(roomId, uid)) continue;
            ChatRoomMember m = new ChatRoomMember();
            m.setRoomId(roomId); m.setUserId(uid); m.setRole(ChatMemberRole.MEMBER);
            memberRepo.save(m);
        }
    }



    @Transactional
    public void removeMember(Long roomId, Long targetUserId) {
        Long me = current.currentUserId();
        // OWNER만 다른 사람 추방 가능(자기 자신은 나가기 API로)
        var my = memberRepo.findById(new ChatRoomMemberPK(){{
            setRoomId(roomId); setUserId(me);
        }}).orElseThrow(() -> new SecurityException("not a member"));
        if (my.getRole() != ChatMemberRole.OWNER) throw new SecurityException("owner only");
        if (Objects.equals(me, targetUserId)) throw new IllegalArgumentException("use leaveRoom");

        memberRepo.deleteById(new ChatRoomMemberPK(){{
            setRoomId(roomId); setUserId(targetUserId);
        }});
    }

    @Transactional
    public void leaveRoom(Long roomId) {
        Long me = current.currentUserId();
        var meRow = memberRepo.findById(new ChatRoomMemberPK(){{
            setRoomId(roomId); setUserId(me);
        }}).orElseThrow(() -> new SecurityException("not a member"));

        // 마지막 OWNER가 나가면: (정책 택1)
        // 1) 남은 멤버 중 한 명에게 OWNER 위임
        // 2) 방을 아카이브/삭제
        // 여기선 간단히 나가기만 처리
        memberRepo.delete(meRow);
    }

}