package amgw.amgw.repository;

import amgw.amgw.chat.model.ChatMessageRead;
import amgw.amgw.chat.model.ChatMessageReadPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, ChatMessageReadPK> { }