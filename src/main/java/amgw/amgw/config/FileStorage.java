package amgw.amgw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileStorage {
    @Value("${app.storage.root:/uploads}")
    private String root;

    public String saveDoc(MultipartFile file, String ext) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String rel = String.format("files/docs/%s.%s", uuid, ext);
        Path path = Paths.get(root, rel);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return "/"+rel; // storage_key 로 저장
    }

    public String saveVersionFromUrl(String docUuid, int ver, String ext, String downloadUrl) throws IOException {
        String rel = String.format("files/versions/%s/%d.%s", docUuid, ver, ext);
        Path path = Paths.get(root, rel);
        Files.createDirectories(path.getParent());
        try (InputStream in = new URL(downloadUrl).openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/"+rel;
    }
}

