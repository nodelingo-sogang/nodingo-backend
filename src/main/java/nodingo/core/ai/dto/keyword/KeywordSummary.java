package nodingo.core.ai.dto.keyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class KeywordSummary {

    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private List<SummaryKeywordInput> keywords;
        private List<SummaryNewsInput> relatedNews;
        private String userPersona;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private String summary; // AI가 생성한 최종 브리핑 본문
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
}