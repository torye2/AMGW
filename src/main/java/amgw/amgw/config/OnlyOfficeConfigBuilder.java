package amgw.amgw.config;

import amgw.amgw.entity.Documents;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnlyOfficeConfigBuilder {
    @Value("${app.onlyoffice.server-base-url}")
    private String ooBase;

    @Value("${app.onlyoffice.jwt.enabled:true}")
    private boolean jwtEnabled;
    @Value("${app.onlyoffice.jwt.secret:change-me}")
    private String jwtSecret;

    public Map<String, Object> build(Documents doc, String docUuid, String fileType, String callbackUrl, String userId, String userName, boolean canEdit) {
        Map<String, Object> conf = new LinkedHashMap<>();
        conf.put("type", "desktop");
        conf.put("documentType", mapDocType(fileType)); // word/cell/slide

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("fileType", fileType);
        document.put("key", doc.getDocId()+"-"+doc.getVersion()); // 캐싱 키
        document.put("title", doc.getTitle());
        document.put("url", publicUrlFromStorageKey(doc.getStorageKey())); // 정적 서빙 주소
        document.put("permissions", Map.of(
                "edit", canEdit,
                "download", true,
                "print", true,
                "comment", true
        ));
        conf.put("document", document);

        Map<String, Object> editorConfig = new LinkedHashMap<>();
        editorConfig.put("callbackUrl", callbackUrl);
        editorConfig.put("lang", "ko");
        editorConfig.put("user", Map.of("id", userId, "name", userName));
        conf.put("editorConfig", editorConfig);

        if (jwtEnabled) {
            String token = Jwts.builder()
                    .setClaims(conf)
                    .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .compact();
            conf.put("token", token);
        }

        return conf;
    }

    private String publicUrlFromStorageKey(String storageKey){
        // 예: /uploads 가 Caddy로 https://portal.localhost/uploads 로 노출된다고 가정
        return storageKey; // 리버스 프록시에서 /uploads/** 서빙
    }

    private String mapDocType(String fileType){
        String ft = fileType.toLowerCase();
        if (List.of("doc","docx","rtf","odt","txt").contains(ft)) return "word";
        if (List.of("xls","xlsx","ods","csv").contains(ft)) return "cell";
        if (List.of("ppt","pptx","odp").contains(ft)) return "slide";
        return "word";
    }
}

