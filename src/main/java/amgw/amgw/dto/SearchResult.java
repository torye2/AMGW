// src/main/java/amgw/amgw/search/dto/SearchResult.java
package amgw.amgw.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchResult {
    private String type;     // user|approval|chat|notice|doc ...
    private String title;    // 결과 타이틀
    private String subtitle; // 추가설명(부서/상태 등)
    private String url;      // 이동 링크
    private String icon;     // 프론트에서 사용할 아이콘 힌트 (선택)
}
