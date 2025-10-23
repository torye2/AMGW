package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name="document_comments",
        indexes = @Index(name="ix_comment_doc", columnList="doc_id, created_at DESC"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable=false) private Long docId;
    @Column(nullable=false) private Long authorId;
    @Lob @Column(nullable=false) private String content;

    @Column(nullable=false) private LocalDateTime createdAt;
    @PrePersist void prePersist(){ createdAt = LocalDateTime.now(); }
}

