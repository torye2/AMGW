package amgw.amgw.service;

import amgw.amgw.config.FileStorage;
import amgw.amgw.dto.DocumentSummaryDto;
import amgw.amgw.entity.DocumentVersion;
import amgw.amgw.entity.Documents;
import amgw.amgw.repository.DocumentVersionRepository;
import amgw.amgw.repository.DocumentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {
    private final DocumentsRepository docsRepo;
    private final DocumentVersionRepository verRepo;
    private final FileStorage storage;

    public Documents create(Long ownerId, String title, MultipartFile file) throws IOException {
        String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename()))
                .orElse("docx");
        String storageKey = storage.saveDoc(file, ext);

        Documents doc = Documents.builder()
                .ownerId(ownerId)
                .title(title)
                .mimeType(file.getContentType())
                .sizeBytes(file.getSize())
                .storageKey(storageKey)
                .version(1)
                .lockState(0)
                .build();
        docsRepo.save(doc);

        // 초기 버전 레코드
        verRepo.save(DocumentVersion.builder()
                .docId(doc.getDocId())
                .version(1)
                .editorId(ownerId)
                .storageKey(storageKey)
                .changeNote("Initial upload")
                .build());

        return doc;
    }

    @Transactional(readOnly = true)
    public Page<DocumentSummaryDto> listMine(Long ownerId, Pageable pageable) {
        return docsRepo.findByOwnerIdOrderByUpdatedAtDesc(ownerId, pageable)
                .map(d -> DocumentSummaryDto.builder()
                        .docId(d.getDocId())
                        .title(d.getTitle())
                        .mimeType(d.getMimeType())
                        .sizeBytes(d.getSizeBytes())
                        .version(d.getVersion())
                        .lockState(d.getLockState())
                        .updatedAt(d.getUpdatedAt())
                        .build());
    }

    @Transactional(readOnly = true)
    public Documents get(Long docId) {
        return (Documents) docsRepo.findById(docId).orElseThrow(() -> new NoSuchElementException("doc not found"));
    }

    public Documents rename(Long docId, String title, Long folderId) {
        Documents d = get(docId);
        d.setTitle(title);
        if (folderId != null) d.setFolderId(folderId);
        return d;
    }

    /** OnlyOffice 콜백에서 새 버전 반영 */
    public void addVersionFromOnlyOffice(Long docId, Long editorId, String docUuid, String ext, String downloadUrl, String changeNote) throws IOException {
        Documents doc = get(docId);
        int nextVer = doc.getVersion() + 1;
        String storageKey = storage.saveVersionFromUrl(docUuid, nextVer, ext, downloadUrl);

        verRepo.save(DocumentVersion.builder()
                .docId(docId)
                .version(nextVer)
                .editorId(editorId != null ? editorId : doc.getOwnerId())
                .storageKey(storageKey)
                .changeNote(changeNote)
                .build());

        doc.setVersion(nextVer);
        doc.setStorageKey(storageKey);
        doc.setUpdatedAt(LocalDateTime.now());
        doc.setSizeBytes(Files.size(Paths.get(
                StringUtils.trimLeadingCharacter(storageKey, '/')))); // 선택: size 갱신
    }
}

