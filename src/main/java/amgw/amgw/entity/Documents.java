package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Documents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docId;

    @Column(nullable = false) private Long ownerId;
    private Long folderId;

    @Column(nullable = false, length = 255) private String title;
    @Column(nullable = false, length = 512) private String storageKey; // /files/docs/{uuid}.docx
    @Column(nullable = false, length = 128) private String mimeType;
    @Column(nullable = false) private Long sizeBytes;

    @Column(nullable = false) private Integer version = 1;
    @Column(nullable = false) private Integer lockState = 0;

    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    @PrePersist void prePersist(){
        createdAt = updatedAt = LocalDateTime.now();
    }
    @PreUpdate void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}

