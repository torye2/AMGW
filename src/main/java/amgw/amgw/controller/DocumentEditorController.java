package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.config.OnlyOfficeConfigBuilder;
import amgw.amgw.entity.Documents;
import amgw.amgw.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocumentEditorController {
    private final DocumentService docs;
    private final OnlyOfficeConfigBuilder builder;

    @GetMapping("/{docId}/editor-config")
    public Map<String, Object> editorConfig(@PathVariable Long docId, HttpServletRequest req) {
        Documents d = docs.get(docId);

        String fileType = Optional.ofNullable(StringUtils.getFilenameExtension(d.getStorageKey()))
                .orElse("docx");
        String userId = String.valueOf(((CustomUserDetails)req.getSession().getAttribute("me")).getUserId());
        String userName = /* 로그인 사용자 닉네임 등 */ Optional.ofNullable(((CustomUserDetails)req.getSession().getAttribute("me")).getName()).orElse("User");

        String callback = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/docs/{id}/onlyoffice/callback")
                .buildAndExpand(docId).toString();

        boolean canEdit = true; // 이후 share/role 체크로 대체

        Map<String, Object> conf = builder.build(d,
                /*docUuid*/ Paths.get(d.getStorageKey()).getFileName().toString().split("\\.")[0],
                fileType, callback, userId, userName, canEdit);

        return conf;
    }
}

