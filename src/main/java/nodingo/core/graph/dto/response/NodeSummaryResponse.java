package nodingo.core.graph.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.graph.dto.result.NodeSummaryResult;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeSummaryResponse {
    private Long keywordId;
    private String word;
    private String persona;
    private String summary;

    public static NodeSummaryResponse from(NodeSummaryResult result) {
        return NodeSummaryResponse.builder()
                .keywordId(result.getKeywordId())
                .word(result.getWord())
                .persona(result.getPersona())
                .summary(result.getSummary())
                .build();
    }
}