package amgw.amgw.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DocumentUpdateReq {
    @NotBlank private String title;
    private Long folderId;
}
