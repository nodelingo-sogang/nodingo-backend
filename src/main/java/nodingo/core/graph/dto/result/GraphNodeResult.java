package nodingo.core.graph.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.ai.dto.graphPreview.GraphPreview;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphNodeResult {
    private Long id;
    private String label;
    private double score;
    private String summary;
    private String persona;

    public static GraphNodeResult from(GraphPreview.GraphNode node) {
        return GraphNodeResult.builder()
                .id(node.getId())
                .label(node.getLabel())
                .score(node.getScore())
                .summary(node.getSummary())
                .persona(node.getPersona())
                .build();
    }
}
