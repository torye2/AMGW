package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.dto.DocumentCreateReq;
import amgw.amgw.dto.DocumentSummaryDto;
import amgw.amgw.dto.DocumentUpdateReq;
import amgw.amgw.entity.Documents;
import amgw.amgw.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> upload(@RequestPart("meta") @Valid DocumentCreateReq meta,
                                     @RequestPart("file") MultipartFile file,
                                     HttpServletRequest req) throws IOException {
        Long uid = ((CustomUserDetails)req.getSession().getAttribute("me")).getUserId();
        Documents d = service.create(uid, meta.getTitle(), file);
        return Map.of("ok", true, "docId", d.getDocId());
    }

    @GetMapping
    public Page<DocumentSummaryDto> list(Pageable pageable, HttpServletRequest req) {
        Long uid = ((CustomUserDetails)req.getSession().getAttribute("me")).getUserId();
        return service.listMine(uid, pageable);
    }

    @GetMapping("/{docId}")
    public Documents get(@PathVariable Long docId) {
        return service.get(docId);
    }

    @PatchMapping("/{docId}")
    public Map<String,Object> update(@PathVariable Long docId, @RequestBody @Valid DocumentUpdateReq body) {
        service.rename(docId, body.getTitle(), body.getFolderId());
        return Map.of("ok", true);
    }
}

