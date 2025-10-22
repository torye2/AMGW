package amgw.amgw.repository;

import amgw.amgw.approvals.model.ApprovalDoc;
import amgw.amgw.approvals.model.ApprovalDoc.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApprovalDocRepository extends JpaRepository<ApprovalDoc, Long> {

    List<ApprovalDoc> findTop50ByDrafterIdOrderByCreatedAtDesc(Long drafterId);
    List<ApprovalDoc> findTop50ByApproverIdAndStatusOrderByCreatedAtDesc(Long approverId, Status status);

    @Query(
            value = """
            select d from ApprovalDoc d
            where (d.drafterId = :userId or d.approverId = :userId)
              and lower(coalesce(d.title, '')) like lower(:kw)
            order by d.createdAt desc
        """,
            countQuery = """
            select count(d) from ApprovalDoc d
            where (d.drafterId = :userId or d.approverId = :userId)
              and lower(coalesce(d.title, '')) like lower(:kw)
        """
    )
    Page<ApprovalDoc> searchVisibleToUser(
            @Param("userId") Long userId,
            @Param("kw") String kw,
            Pageable pageable
    );
}
