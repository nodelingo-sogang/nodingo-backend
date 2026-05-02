package nodingo.core.ai.dto.userEmbedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserEmbedding {

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class InitRequest {
        private Long userId;
        private List<InterestKeyword> interestKeywords;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        private Long userId;
        private float[] oldEmbedding;
        private List<Activity> activities;
        private double decay;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long userId;
        private float[] embedding;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class InterestKeyword {
        private Long keywordId;
        private String word;
        private float[] embedding;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Activity {
        private String type; // SCRAP, CLICK
        private Long newsId;
        private float[] newsEmbedding;
        private Long keywordId;
        private float[] keywordEmbedding;
        private double weight;
    }
}