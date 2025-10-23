package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.entity.DocumentShare;
import amgw.amgw.repository.DocumentShareRepository;
import amgw.amgw.repository.DocumentsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/docs/{docId}/shares")
@RequiredArgsConstructor
public class DocumentShareController {
    private final DocumentShareRepository repo;

    @GetMapping
    public List<DocumentShare> list(@PathVariable Long docId) { return repo.findByDocId(docId); }

    @PostMapping
    public DocumentShare add(@PathVariable Long docId, @RequestBody Map<String,Object> body, HttpServletRequest req){
        Long uid = ((CustomUserDetails)req.getSession().getAttribute("loginUser")).getUserId();
        DocumentShare s = DocumentShare.builder()
                .docId(docId)
                .subjectType(DocumentShare.SubjectType.valueOf(((String)body.get("subjectType")).toUpperCase()))
                .subjectId(body.get("subjectId")==null?null:Long.valueOf(body.get("subjectId").toString()))
                .role(DocumentShare.Role.valueOf(((String)body.get("role")).toUpperCase()))
                .linkToken(UUID.randomUUID().toString().replace("-","").substring(0,43))
                .createdBy(uid)
                .expiresAt(null)
                .build();
        return repo.save(s);
    }

    @DeleteMapping("/{shareId}")
    public void remove(@PathVariable Long shareId){ repo.deleteById(shareId); }
}

