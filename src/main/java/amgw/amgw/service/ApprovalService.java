package amgw.amgw.service;

import amgw.amgw.approvals.model.ApprovalDoc;
import amgw.amgw.repository.ApprovalDocRepository;
import amgw.amgw.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service @RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalDocRepository repo;
    private final CurrentUserService current;

    @Transactional
    public ApprovalDoc createAndSubmit(String title, String body, Long approverId, ApprovalDoc.DocType type){
        var doc = ApprovalDoc.builder()
                .title(title)
                .body(body)
                .docType(type!=null?type: ApprovalDoc.DocType.GENERAL)
                .drafterId(current.currentUserId())
                .approverId(approverId)
                .status(ApprovalDoc.Status.SUBMITTED)
                .build();
        return repo.save(doc);
    }

    public List<ApprovalDoc> myDraftsOrSubmitted(){
        return repo.findTop50ByDrafterIdOrderByCreatedAtDesc(current.currentUserId());
    }

    public List<ApprovalDoc> myPendingToApprove(){
        return repo.findTop50ByApproverIdAndStatusOrderByCreatedAtDesc(
                current.currentUserId(), ApprovalDoc.Status.SUBMITTED);
    }

    @Transactional
    public ApprovalDoc approve(Long id){
        var doc = repo.findById(id).orElseThrow();
        // 권한 체크(내가 승인자인지)
        if(!doc.getApproverId().equals(current.currentUserId()))
            throw new IllegalStateException("승인 권한이 없습니다.");
        doc.setStatus(ApprovalDoc.Status.APPROVED);
        return doc;
    }

    @Transactional
    public ApprovalDoc reject(Long id){
        var doc = repo.findById(id).orElseThrow();
        if(!doc.getApproverId().equals(current.currentUserId()))
            throw new IllegalStateException("반려 권한이 없습니다.");
        doc.setStatus(ApprovalDoc.Status.REJECTED);
        return doc;
    }

    @Transactional(readOnly = true)
    public ApprovalDoc getByIdOrThrow(Long id) {
        ApprovalDoc doc = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        Long me = current.currentUserId();
        // 기안자도 아니고 승인자도 아니면 403
        if (!me.equals(doc.getDrafterId()) && !me.equals(doc.getApproverId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "열람 권한이 없습니다.");
        }
        return doc;
    }
}
