package nodingo.core.graph.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.graph.dto.result.TabListResult;
import nodingo.core.graph.dto.result.TabResult;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabListResponse {
    private List<TabResult> tabs;

    public static TabListResponse of(TabListResult result) {
        return TabListResponse.builder()
                .tabs(result.getTabs())
                .build();
    }
}