package nodingo.core.ai.dto.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class NewsRelationAnalysis {

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private List<NewsEmbeddingInput> newsEmbeddings;
        private Double similarityThreshold;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private List<RelationResult> relations;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class NewsEmbeddingInput {
        private Long newsId;
        private float[] embedding;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class RelationResult {
        private Long subjectNewsId;
        private Long relatedNewsId;
        private Double relationScore;
    }
}