package amgw.amgw.repository;

import amgw.amgw.entity.DocumentComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {
    Page<DocumentComment> findByDocIdOrderByCreatedAtDesc(Long docId, Pageable pageable);
}

