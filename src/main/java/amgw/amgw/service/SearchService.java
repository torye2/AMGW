package amgw.amgw.service;

import amgw.amgw.repository.*;
import amgw.amgw.chat.model.ChatRoom;
import amgw.amgw.chat.model.ChatEnums;
import amgw.amgw.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CurrentUserService current;
    private final UserRepository userRepo;
    private final ApprovalDocRepository approvalRepo;
    private final ChatRoomRepository chatRoomRepo;
    private final ChatRoomMemberRepository chatRoomMemberRepo;

    public List<SearchResult> search(String q) {
        final String query = Optional.ofNullable(q).orElse("").trim();
        if (query.isEmpty()) return List.of();

        Long me = current.currentUserId();
        List<SearchResult> out = new ArrayList<>();

        // 1) 사용자 (상위 5)
        out.addAll(
                userRepo.searchByNameOrUsername(like(query), PageRequest.of(0, 5)).stream()
                        .map(u -> SearchResult.builder()
                                .type("user")
                                .title(u.getName() + " · 사번 " + u.getId())  // 사번 표기
                                .subtitle(
                                        Optional.ofNullable(u.getDepartment()).filter(s->!s.isBlank()).orElse("부서 미지정")
                                                + " · 계정 " + u.getUsername()
                                )
                                .url(null)              // 사람은 이동 링크 없음
                                .icon("user")
                                .build())
                        .toList()
        );
        // 2) 결재 (상위 5)
        out.addAll(
                approvalRepo.searchVisibleToUser(me, like(query), PageRequest.of(0, 5))
                        .map(d -> SearchResult.builder()
                                .type("approval")
                                .title(d.getTitle())
                                .subtitle("상태: " + d.getStatus() + " · 종류: " + d.getDocType())
                                .url("/approvals/" + d.getId())
                                .icon("approval")
                                .build())
                        .toList()
        );

        // 3) 채팅방 (내가 멤버인 방 + 이름 매칭, 상위 5)
        var myRoomIds = chatRoomMemberRepo.findByUserId(me).stream()
                .map(m -> m.getRoomId())
                .collect(Collectors.toSet());

        if (!myRoomIds.isEmpty()) {
            out.addAll(
                    chatRoomRepo.searchByNameInIds(like(query), myRoomIds, PageRequest.of(0, 5))
                            .stream()
                            .map(r -> SearchResult.builder()
                                    .type("chat")
                                    .title(roomTitle(r))
                                    .subtitle(r.getType() == ChatEnums.ChatRoomType.GROUP ? "그룹방" : "1:1 채팅")
                                    .url("/chat?roomId=" + r.getId())
                                    .icon("chat")
                                    .build())
                            .toList()
            );
        }

        // 최종 상위 12개로 제한
        return out.stream().limit(12).toList();
    }

    private String like(String q) { return "%" + q + "%"; }

    private String roomTitle(ChatRoom r) {
        return Optional.ofNullable(r.getName()).filter(s -> !s.isBlank())
                .orElse("채팅방 #" + r.getId());
    }
}
