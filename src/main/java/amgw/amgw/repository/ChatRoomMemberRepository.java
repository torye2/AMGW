package amgw.amgw.repository;

import amgw.amgw.chat.model.ChatRoomMember;
import amgw.amgw.chat.model.ChatRoomMemberPK;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberPK> {
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
    List<ChatRoomMember> findByUserId(Long userId);
}