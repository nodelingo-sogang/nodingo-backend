package nodingo.core.graph.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.keyword.domain.RecommendKeyword;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabResult {
    private Long keywordId;
    private String word;
    private String persona;

    public static TabResult of(RecommendKeyword rk) {
        return TabResult.builder()
                .keywordId(rk.getKeyword().getId())
                .word(rk.getKeyword().getWord())
                .persona(rk.getKeyword().getPersona() != null ? rk.getKeyword().getPersona().name() : null)
                .build();
    }
}