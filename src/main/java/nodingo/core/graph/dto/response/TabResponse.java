package nodingo.core.graph.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.graph.dto.result.TabResult;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabResponse {
    private TabResult tab;

    public static TabResponse of(TabResult result) {
        return TabResponse.builder()
                .tab(result)
                .build();
    }
}