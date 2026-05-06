package nodingo.core.graph.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.graph.dto.result.GraphDataResult;
import nodingo.core.graph.dto.result.GraphEdgeResult;
import nodingo.core.graph.dto.result.GraphNodeResult;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphDataResponse {
    private List<GraphNodeResult> nodes;
    private List<GraphEdgeResult> edges;

    public static GraphDataResponse from(GraphDataResult result) {
        return GraphDataResponse.builder()
                .nodes(result.getNodes())
                .edges(result.getEdges())
                .build();
    }
}
