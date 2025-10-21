package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name="document_shares",
        indexes = {
                @Index(name="ix_share_doc", columnList="doc_id"),
                @Index(name="ix_share_subject", columnList="subject_type,subject_id"),
                @Index(name="ix_share_token", columnList="link_token")
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentShare {
    public enum SubjectType { USER, DEPT, LINK }
    public enum Role { OWNER, EDIT, VIEW, COMMENT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @Column(nullable=false) private Long docId;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=8)
    private SubjectType subjectType;

    private Long subjectId;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=8)
    private Role role;

    @Column(length=43) private String linkToken;
    private LocalDateTime expiresAt;

    @Column(nullable=false) private Long createdBy;
    @Column(nullable=false) private LocalDateTime createdAt;

    @PrePersist void prePersist(){ createdAt = LocalDateTime.now(); }
}

