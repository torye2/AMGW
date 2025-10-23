package amgw.amgw.controller;

import amgw.amgw.entity.Documents;
import amgw.amgw.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docs/{docId}/onlyoffice")
@RequiredArgsConstructor
public class OnlyOfficeCallbackController {
    private final DocumentService docs;
    @Value("${app.onlyoffice.jwt.enabled:true}") boolean jwtEnabled;
    @Value("${app.onlyoffice.jwt.secret:change-me}") String jwtSecret;

    @PostMapping("/callback")
    public Map<String,Object> callback(@PathVariable Long docId, @RequestBody Map<String, Object> payload) throws IOException {
        // (선택) JWT 검증
        // status: 1-editing, 2-saved, 6-forcesaved
        int status = ((Number) payload.getOrDefault("status", 0)).intValue();
        if (status == 2 || status == 6) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> urls = (List<Map<String, String>>) payload.get("url");
            String url = urls != null && !urls.isEmpty() ? urls.get(0).get("url") : (String) payload.get("url");
            String userId = String.valueOf(payload.getOrDefault("userid", ""));
            String changeNote = (String) payload.getOrDefault("changesurl", "saved");

            // 파일 확장자 추출
            // 실무에서는 기존 title / mime 으로 판단
            String ext = "docx";
            Documents doc = docs.get(docId);
            String docUuid = Paths.get(doc.getStorageKey()).getFileName().toString().split("\\.")[0];

            docs.addVersionFromOnlyOffice(docId, userId.isEmpty()?null:Long.valueOf(userId),
                    docUuid, ext, url, changeNote);
            return Map.of("error", 0);
        }
        return Map.of("error", 0);
    }
}

