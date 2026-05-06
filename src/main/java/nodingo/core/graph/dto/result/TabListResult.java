package nodingo.core.graph.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabListResult {
    private List<TabResult> tabs;

    public static TabListResult of(List<TabResult> tabs) {
        return TabListResult.builder()
                .tabs(tabs)
                .build();
    }
}