package amgw.amgw.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentSummaryDto {
    private Long docId;
    private String title;
    private String mimeType;
    private Long sizeBytes;
    private Integer version;
    private Integer lockState;
    private LocalDateTime updatedAt;
}
