package nodingo.core.ai.dto.keyword;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class KeywordRecommend {
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private Long userId;
        private float[] userEmbedding;
        private List<CandidateKeyword> candidateKeywords;
        private LocalDate targetDate;
        private int topK;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private List<RecommendResult> recommendKeywords;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class CandidateKeyword {
        private Long keywordId;
        private String word;
        private String normalizedWord;
        private float[] embedding;
        private double recentImportance;
        private boolean isUserInterest;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class RecommendResult {
        private Long userId;
        private Long keywordId;
        private LocalDate targetDate;
        private double score;
        private String summary;
    }
}