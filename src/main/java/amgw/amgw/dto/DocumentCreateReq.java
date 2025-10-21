package amgw.amgw.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCreateReq {
    @NotBlank
    private String title;
    // MultipartFile file 은 컨트롤러 @RequestPart 로 받음
}
