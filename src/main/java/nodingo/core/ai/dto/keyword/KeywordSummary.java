package nodingo.core.ai.dto.keyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class KeywordSummary {

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private Long userId;
        private SummaryKeywordInput keyword;
        private List<SummaryNewsInput> relatedNews;
        private List<SummaryRelatedKeywordInput> relatedKeywords;
        private LocalDate targetDate;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long userId;
        private Long keywordId;
        private LocalDate targetDate;
        private String summary;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class SummaryKeywordInput {
        private Long keywordId;
        private String word;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class SummaryNewsInput {
        private Long newsId;
        private String title;
        private String body;
    }

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class SummaryRelatedKeywordInput {
        private Long keywordId;
        private String word;
    }
}