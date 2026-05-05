package nodingo.core.ai.dto.graphPreview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


public class GraphPreview {

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private List<GraphRecommendKeywordInput> recommendKeywords;
        private List<GraphKeywordRelationInput> keywordRelations;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private List<GraphNode> nodes;
        private List<GraphEdge> edges;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class GraphRecommendKeywordInput {
        private Long keywordId;
        private String word;
        private double score;
        private String summary;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class GraphKeywordRelationInput {
        private Long sourceKeywordId;
        private Long targetKeywordId;
        private double relationScore;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class GraphNode {
        private Long id;
        private String label;
        private double score;
        private String summary;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class GraphEdge {
        private Long source;
        private Long target;
        private double weight;
    }
}