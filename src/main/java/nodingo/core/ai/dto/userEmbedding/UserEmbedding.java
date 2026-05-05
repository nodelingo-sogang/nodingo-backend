package nodingo.core.ai.dto.userEmbedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.news.domain.News;

import java.util.List;

public class UserEmbedding {

    /**
     * 초기 온보딩용 (관심 키워드 기반)
     */
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class InitRequest {
        private Long userId;
        private List<InterestKeyword> interestKeywords;
    }

    /**
     * 유저 스크랩 활동 기반 업데이트 요청
     */
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        private Long userId;
        private float[] oldEmbedding;
        private List<Activity> activities;
        private double decay;
    }

    @Getter @Builder
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
        private String type;
        private Long newsId;
        private float[] newsEmbedding;
        private double weight;

        public static Activity createScrap(News news, double weight) {
            return Activity.builder()
                    .type("SCRAP")
                    .newsId(news.getId())
                    .newsEmbedding(news.getEmbedding())
                    .weight(weight)
                    .build();
        }
    }
}