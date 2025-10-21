package amgw.amgw.service;

import amgw.amgw.approvals.model.ApprovalDoc;
import amgw.amgw.repository.ApprovalDocRepository;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalDocRepository repo;
    private final CurrentUserService current;
    private final NotificationService notificationService; // 🔔 알림
    private final UserRepository userRepo;                 // 표시 이름 조회

    /** 상신 + 즉시 제출 */
    @Transactional
    public ApprovalDoc createAndSubmit(String title, String body, Long approverId, ApprovalDoc.DocType type){
        var doc = ApprovalDoc.builder()
                .title(title)
                .body(body)
                .docType(type != null ? type : ApprovalDoc.DocType.GENERAL)
                .drafterId(current.currentUserId())
                .approverId(approverId)
                .status(ApprovalDoc.Status.SUBMITTED)
                .build();

        doc = repo.save(doc); // ID 필요하므로 먼저 저장

        // 🔔 승인자에게 결재 요청 알림
        try {
            Long drafterId = doc.getDrafterId();
            String drafterNm = userRepo.findNameById(drafterId);
            notificationService.pushNotification(
                    approverId,
                    "approval",
                    (drafterNm != null ? drafterNm : "사용자") + "님이 결재 요청을 보냈습니다",
                    Map.of(
                            "requesterName", drafterNm,
                            "title", doc.getTitle(),
                            "docId", doc.getId()
                    )
            );
        } catch (Exception ignore) {}

        return doc;
    }

    /** 내가 올린 문서 (기안/상신) */
    @Transactional(readOnly = true)
    public List<ApprovalDoc> myDraftsOrSubmitted(){
        return repo.findTop50ByDrafterIdOrderByCreatedAtDesc(current.currentUserId());
    }

    /** 내가 결재해야 할 문서 (대기) */
    @Transactional(readOnly = true)
    public List<ApprovalDoc> myPendingToApprove(){
        return repo.findTop50ByApproverIdAndStatusOrderByCreatedAtDesc(
                current.currentUserId(), ApprovalDoc.Status.SUBMITTED);
    }

    /** 승인 */
    @Transactional
    public ApprovalDoc approve(Long id){
        var doc = repo.findById(id).orElseThrow();
        if (!doc.getApproverId().equals(current.currentUserId()))
            throw new IllegalStateException("승인 권한이 없습니다.");

        doc.setStatus(ApprovalDoc.Status.APPROVED);

        // 🔔 기안자에게 승인 알림
        try {
            String approverNm = userRepo.findNameById(doc.getApproverId());
            notificationService.pushNotification(
                    doc.getDrafterId(),
                    "approval",
                    (approverNm != null ? approverNm : "승인자") + "님이 결재를 승인했습니다",
                    Map.of(
                            "requesterName", approverNm,
                            "title", doc.getTitle(),
                            "docId", doc.getId(),
                            "result", "APPROVED"
                    )
            );
        } catch (Exception ignore) {}

        return doc;
    }

    /** 반려 */
    @Transactional
    public ApprovalDoc reject(Long id){
        var doc = repo.findById(id).orElseThrow();
        if (!doc.getApproverId().equals(current.currentUserId()))
            throw new IllegalStateException("반려 권한이 없습니다.");

        doc.setStatus(ApprovalDoc.Status.REJECTED);

        // 🔔 기안자에게 반려 알림
        try {
            String approverNm = userRepo.findNameById(doc.getApproverId());
            notificationService.pushNotification(
                    doc.getDrafterId(),
                    "approval",
                    (approverNm != null ? approverNm : "승인자") + "님이 결재를 반려했습니다",
                    Map.of(
                            "requesterName", approverNm,
                            "title", doc.getTitle(),
                            "docId", doc.getId(),
                            "result", "REJECTED"
                    )
            );
        } catch (Exception ignore) {}

        return doc;
    }

    /** 권한 검증 포함 단건 조회 */
    @Transactional(readOnly = true)
    public ApprovalDoc getByIdOrThrow(Long id) {
        ApprovalDoc doc = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        Long me = current.currentUserId();
        if (!me.equals(doc.getDrafterId()) && !me.equals(doc.getApproverId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "열람 권한이 없습니다.");
        }
        return doc;
    }
}
