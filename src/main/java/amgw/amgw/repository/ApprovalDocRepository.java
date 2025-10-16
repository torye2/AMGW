package amgw.amgw.repository;

import amgw.amgw.approvals.model.ApprovalDoc;
import amgw.amgw.approvals.model.ApprovalDoc.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalDocRepository extends JpaRepository<ApprovalDoc, Long> {
    List<ApprovalDoc> findTop50ByDrafterIdOrderByCreatedAtDesc(Long drafterId);
    List<ApprovalDoc> findTop50ByApproverIdAndStatusOrderByCreatedAtDesc(Long approverId, Status status);
}