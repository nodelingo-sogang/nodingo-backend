package nodingo.core.ai.client;

import nodingo.core.ai.dto.graphPreview.GraphPreview;
import nodingo.core.ai.dto.keyword.KeywordRecommend;
import nodingo.core.ai.dto.keyword.KeywordSummary;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.ai.dto.relation.NewsRelationAnalysis;
import nodingo.core.ai.dto.userEmbedding.UserEmbedding;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "aiClient", url = "${ai.server.url}")
public interface AiClient {

    /**
     * 1. 뉴스 분석 및 키워드 추출 (배치용)
     */
    @PostMapping("/v1/news/analyze-batch")
    NewsBatch.Response analyzeNewsBatch(@RequestBody NewsBatch.Request request);

    /**
     * 2. 뉴스 간의 관계 생성 (뉴스 상세 페이지나 추천용)
     */
    @PostMapping("/v1/news/build-news-relations")
    NewsRelationAnalysis.Response buildNewsRelations(@RequestBody NewsRelationAnalysis.Request request);

    /**
     * 3. 유저 최초 온보딩 시 임베딩 초기화
     */
    @PostMapping("/v1/users/init-embedding")
    UserEmbedding.Response initUserEmbedding(@RequestBody UserEmbedding.InitRequest request);

    /**
     * 4. 유저 활동(스크랩, 클릭) 기반 임베딩 업데이트
     */
    @PostMapping("/v1/users/update-embedding")
    UserEmbedding.Response updateUserEmbedding(@RequestBody UserEmbedding.UpdateRequest request);

    /**
     * 5. 개인화 키워드 추천 스코어 계산
     */
    @PostMapping("/v1/recommend-keywords")
    KeywordRecommend.Response recommendKeywords(@RequestBody KeywordRecommend.Request request);

    /**
     * 6. 추천된 키워드들에 대한 AI 브리핑 요약 생성
     */
    @PostMapping("/v1/recommend-keywords/summarize")
    KeywordSummary.Response summarizeKeywords(@RequestBody KeywordSummary.Request request);

    /**
     * 7. 그래프 시각화 데이터 생성
     */
    @PostMapping("/v1/graph/preview")
    GraphPreview.Response getGraphPreview(@RequestBody GraphPreview.Request request);

    /**
     * 8. 헬스 체크
     */
    @GetMapping("/health")
    Map<String, Object> healthCheck();
}
