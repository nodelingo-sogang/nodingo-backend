package nodingo.core.batch.news.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.batch.service.cache.KeywordCacheService;
import nodingo.core.global.util.NewsSummarizer;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.domain.KeywordRelation;
import nodingo.core.keyword.repository.KeywordRelationRepository;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class NewsAiWriter implements ItemWriter<News> {

    private final NewsRepository newsRepository;
    private final KeywordRepository keywordRepository;
    private final KeywordRelationRepository keywordRelationRepository;
    private final AiClient aiClient;
    private final NewsSummarizer newsSummarizer;
    private final KeywordCacheService keywordCacheService;

    private static final int TOP_K_KEYWORDS = 5;

    @Override
    public void write(Chunk<? extends News> items) {
        List<News> chunkItems = new ArrayList<>(items.getItems());

        if (chunkItems.isEmpty()) return;

        // 1. DB 1차 저장 (AI 서버에 보낼 ID 확보)
        List<News> savedNews = newsRepository.saveAll(chunkItems);

        Map<Long, News> newsMap = savedNews.stream()
                .collect(Collectors.toMap(News::getId, Function.identity()));

        // 2. Redis 메모리에서 키워드 가져오기
        List<NewsBatch.ExistingKeywordInput> existingKeywords = keywordCacheService.getAllKeywords();

        // 3. AI 분석 요청 객체 생성 (Chunk 단위 통째로 요청)
        NewsBatch.Request request = NewsBatch.Request.builder()
                .news(savedNews.stream()
                        .map(n -> NewsBatch.NewsInput.builder()
                                .newsId(n.getId())
                                .title(n.getTitle())
                                .body(n.getBody())
                                .build())
                        .toList())
                .existingKeywords(existingKeywords)
                .topKKeywords(TOP_K_KEYWORDS)
                .build();

        log.info(">>>> [Batch Writer] Requesting AI analysis for {} news articles...", savedNews.size());

        // 4. FastAPI 서버 호출
        NewsBatch.Response aiResponse = aiClient.analyzeNewsBatch(request);

        // 신규 생성 키워드 로컬 캐시
        Map<String, Keyword> newKeywordCache = new HashMap<>();

        // 5. AI 분석 결과 반영
        for (NewsBatch.NewsAnalysisResult res : aiResponse.getNewsResults()) {
            News news = newsMap.get(res.getNewsId());
            if (news == null) continue;

            news.updateEmbedding(res.getEmbedding());
            String summary = newsSummarizer.summarize(news);
            news.updateBody(summary);

            for (NewsBatch.KeywordAiResult kwRes : res.getKeywords()) {
                Keyword keyword;

                if (kwRes.isNew()) {
                    keyword = newKeywordCache.get(kwRes.getNormalizedWord());
                    if (keyword == null) {
                        keyword = Keyword.create(kwRes.getWord());
                        keyword.updateEmbedding(kwRes.getEmbedding());
                        keyword = keywordRepository.save(keyword);
                        newKeywordCache.put(kwRes.getNormalizedWord(), keyword);
                    }
                } else {
                    keyword = keywordRepository.findById(kwRes.getKeywordId())
                            .orElseGet(() -> keywordRepository.save(Keyword.create(kwRes.getWord())));
                }

                news.addKeyword(keyword, kwRes.getWeight());
            }
        }

        // 6. 뉴스 최종 업데이트
        newsRepository.saveAll(savedNews);
        log.info(">>>> [Batch Writer] Finished analyzing and summarizing {} news articles.", savedNews.size());

        // 7. 키워드 관계 저장
        if (aiResponse.getKeywordRelations() != null && !aiResponse.getKeywordRelations().isEmpty()) {
            List<KeywordRelation> relationsToSave = new ArrayList<>();

            for (NewsBatch.KeywordRelationResult relRes : aiResponse.getKeywordRelations()) {
                if (relRes.getSourceKeywordId() != null && relRes.getTargetKeywordId() != null) {
                    Keyword source = keywordRepository.findById(relRes.getSourceKeywordId()).orElse(null);
                    Keyword target = keywordRepository.findById(relRes.getTargetKeywordId()).orElse(null);

                    if (source != null && target != null) {
                        relationsToSave.add(KeywordRelation.create(source, target, relRes.getRelationScore()));
                    }
                }
            }

            if (!relationsToSave.isEmpty()) {
                keywordRelationRepository.saveAll(relationsToSave);
                log.info(">>>> [Batch Writer] Successfully saved {} keyword relations.", relationsToSave.size());
            }
        }
    }
}