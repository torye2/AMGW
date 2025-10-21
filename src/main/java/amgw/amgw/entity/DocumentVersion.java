package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="document_versions",
        indexes = @Index(name="ix_doc_ver", columnList="doc_id, created_at DESC"))
@Getter
@Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long verId;

    @Column(nullable=false) private Long docId;
    @Column(nullable=false) private Integer version;
    @Column(nullable=false, length=512) private String storageKey; // /files/versions/{docUuid}/{ver}.docx
    @Column(nullable=false) private Long editorId;

    @Column(length=255) private String changeNote;
    @Column(nullable=false) private LocalDateTime createdAt;

    @PrePersist void prePersist(){ createdAt = LocalDateTime.now(); }
}
