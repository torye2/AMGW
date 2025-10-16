package amgw.amgw.controller;

import amgw.amgw.approvals.model.ApprovalDoc;
import amgw.amgw.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalRestController {
    private final ApprovalService service;

    // 기안/제출
    @PostMapping
    public Map<String,Object> create(
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam Long approverId,
            @RequestParam(required=false, defaultValue = "GENERAL") ApprovalDoc.DocType docType
    ){
        var saved = service.createAndSubmit(title, body, approverId, docType);
        return Map.of("ok", true, "id", saved.getId());
    }

    // 내가 기안한 문서
    @GetMapping("/my")
    public List<ApprovalDoc> myDocs(){
        return service.myDraftsOrSubmitted();
    }

    // 내가 결재해야 할 문서
    @GetMapping("/inbox")
    public List<ApprovalDoc> inbox(){
        return service.myPendingToApprove();
    }

    // 승인/반려
    @PostMapping("/{id}/approve")
    public Map<String,Object> approve(@PathVariable Long id){
        var d = service.approve(id);
        return Map.of("ok", true, "status", d.getStatus());
    }

    @PostMapping("/{id}/reject")
    public Map<String,Object> reject(@PathVariable Long id){
        var d = service.reject(id);
        return Map.of("ok", true, "status", d.getStatus());
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ApprovalDoc detail(@PathVariable Long id) {
        return service.getByIdOrThrow(id);
    }
}