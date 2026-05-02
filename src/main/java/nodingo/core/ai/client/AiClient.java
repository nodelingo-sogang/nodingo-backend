package nodingo.core.ai.client;

import nodingo.core.ai.dto.graphPreview.GraphPreview;
import nodingo.core.ai.dto.keywordRecommend.KeywordRecommend;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.ai.dto.userEmbedding.UserEmbedding;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aiClient", url = "${ai.server.url}")
public interface AiClient {

    // 1. 뉴스 배치 분석
    @PostMapping("/v1/news/analyze-batch")
    NewsBatch.Response analyzeNewsBatch(@RequestBody NewsBatch.Request request);

    // 2. 유저 임베딩 업데이트
    @PostMapping("/v1/users/update-embedding")
    UserEmbedding.Response updateUserEmbedding(@RequestBody UserEmbedding.UpdateRequest request);

    // 3. 키워드 추천 스코어링
    @PostMapping("/v1/recommend-keywords")
    KeywordRecommend.Response recommendKeywords(@RequestBody KeywordRecommend.Request request);

    // 4. 그래프 데이터 생성
    @PostMapping("/v1/graph/preview")
    GraphPreview.Response getGraphPreview(@RequestBody GraphPreview.Request request);
}
