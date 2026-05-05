package nodingo.core.ai.dto.userEmbedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.keyword.domain.NewsKeyword;
import nodingo.core.news.domain.News;
import nodingo.core.user.domain.User;

import java.util.List;

public class UserEmbedding {

    /**
     * 초기 온보딩용 임베딩 요청
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class InitRequest {
        private final Long userId;
        private final List<InterestKeyword> interestKeywords;

        public static InitRequest create(Long userId, List<InterestKeyword> interestKeywords) {
            return InitRequest.builder()
                    .userId(userId)
                    .interestKeywords(interestKeywords)
                    .build();
        }
    }

    /**
     * 실시간 활동(스크랩/클릭) 기반 업데이트 요청
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdateRequest {
        private final Long userId;
        private final float[] oldEmbedding;
        private final List<Activity> activities;
        private final double decay;

        public static UpdateRequest create(User user, List<Activity> activities) {
            return UpdateRequest.builder()
                    .userId(user.getId())
                    .oldEmbedding(user.getEmbedding())
                    .activities(activities)
                    .decay(0.95)
                    .build();
        }
    }

    /**
     * AI 서버 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private float[] embedding;
    }

    /**
     * 관심 키워드 정보
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class InterestKeyword {
        private final Long keywordId;
        private final String word;
        private final float[] embedding;

        public static InterestKeyword create(Long keywordId, String word, float[] embedding) {
            return InterestKeyword.builder()
                    .keywordId(keywordId)
                    .word(word)
                    .embedding(embedding)
                    .build();
        }
    }

    /**
     * 활동 정보 (뉴스 + 대표 키워드)
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class Activity {
        private final String type; // SCRAP, CLICK
        private final Long newsId;
        private final float[] newsEmbedding;
        private final Long keywordId;
        private final float[] keywordEmbedding;
        private final double weight;

        public static Activity createScrap(News news, double weight) {
            NewsKeyword representative = news.getNewsKeywords().get(0);

            return Activity.builder()
                    .type("SCRAP")
                    .newsId(news.getId())
                    .newsEmbedding(news.getEmbedding())
                    .keywordId(representative.getKeyword().getId())
                    .keywordEmbedding(representative.getKeyword().getEmbedding())
                    .weight(weight)
                    .build();
        }
    }
}