package nodingo.core.graph.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.ai.dto.graphPreview.GraphPreview;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphDataResult {
    private List<GraphNodeResult> nodes;
    private List<GraphEdgeResult> edges;

    public static GraphDataResult from(GraphPreview.Response response) {
        return GraphDataResult.builder()
                .nodes(response.getNodes().stream().map(GraphNodeResult::from).toList())
                .edges(response.getEdges().stream().map(GraphEdgeResult::from).toList())
                .build();
    }
}