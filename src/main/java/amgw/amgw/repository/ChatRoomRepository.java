package amgw.amgw.repository;

import amgw.amgw.chat.model.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        select r from ChatRoom r
        where r.id in :ids
          and r.name is not null
          and lower(r.name) like lower(:kw)
        order by r.createdAt desc
    """)
    List<ChatRoom> searchByNameInIds(@Param("kw") String kw,
                                     @Param("ids") Collection<Long> ids,
                                     Pageable pageable);
}
