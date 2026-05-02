package nodingo.core.ai.dto.graphPreview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


public class GraphPreview {

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private List<RecommendKeywordInput> recommendKeywords;
        private List<KeywordRelationInput> keywordRelations;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private List<Node> nodes;
        private List<Edge> edges;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class RecommendKeywordInput {
        private Long keywordId;
        private String word;
        private double score;
        private String summary;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class KeywordRelationInput {
        private Long sourceKeywordId;
        private Long targetKeywordId;
        private double relationScore;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Node {
        private Long id;
        private String label;
        private double score;
        private String summary;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Edge {
        private Long source;
        private Long target;
        private double weight;
    }
}
