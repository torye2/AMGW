package amgw.amgw.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EditorConfigDto {
    private Map<String, Object> config; // OnlyOffice DocEditor 설정 JSON 그대로
}
