package nodingo.core.batch.news.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.domain.KeywordRelation;
import nodingo.core.keyword.repository.KeywordRelationRepository;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.keyword.service.query.KeywordQueryService;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.*;

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
    private final KeywordQueryService keywordQueryService;

    private static final int TOP_K_KEYWORDS = 5;

    @Override
    public void write(Chunk<? extends News> items) {
        List<News> chunkItems = new ArrayList<>(items.getItems());

        if (chunkItems.isEmpty()) return;

        // 1. DB 1차 저장 (AI 서버에 보낼 ID 확보)
        List<News> savedNews = newsRepository.saveAll(chunkItems);

        Map<Long, News> newsMap = savedNews.stream()
                .collect(Collectors.toMap(News::getId, Function.identity()));

        // 2. 파이썬 전송용 기존 키워드 목록 비우기 (메모리 및 직렬화 에러 방지)
        List<NewsBatch.ExistingKeywordInput> existingKeywords = new ArrayList<>();

        // 3. AI 분석 요청 객체 생성
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

        log.info(">>>> [Batch Writer] Requesting AI analysis for {} news articles. (Chunk size: {})",
                savedNews.size(), items.size());

        // 4. FastAPI 서버 호출
        NewsBatch.Response aiResponse = aiClient.analyzeNewsBatch(request);

        // 5. AI 분석 결과 반영 (임베딩 및 요약본 업데이트)
        for (NewsBatch.NewsAnalysisResult res : aiResponse.getNewsResults()) {
            News news = newsMap.get(res.getNewsId());
            if (news != null) {
                news.updateEmbedding(res.getEmbedding());
                news.updateBody(res.getSummary()); // 파이썬이 던져준 200자 요약본 반영
            }
        }

        // 6. DB Bulk 조회 준비: 이번 Chunk에서 나온 키워드만 Set으로 추출
        Set<String> extractedWords = aiResponse.getNewsResults().stream()
                .flatMap(res -> res.getKeywords().stream())
                .map(NewsBatch.KeywordAiResult::getNormalizedWord)
                .collect(Collectors.toSet());

        // 7. KeywordQueryService 호출
        Map<String, Keyword> existingKeywordMap = keywordQueryService.getExistingKeywordsMap(extractedWords);

        // 8. 키워드 매핑 및 신규 저장
        for (NewsBatch.NewsAnalysisResult res : aiResponse.getNewsResults()) {
            News news = newsMap.get(res.getNewsId());
            if (news == null) continue;

            for (NewsBatch.KeywordAiResult kwRes : res.getKeywords()) {
                String normWord = kwRes.getNormalizedWord();

                // Map에 있으면 QueryService가 가져온 기존 엔티티 사용, 없으면 새로 저장
                Keyword keyword = existingKeywordMap.computeIfAbsent(normWord, key -> {
                    Keyword newKw = Keyword.create(kwRes.getWord());
                    newKw.updateEmbedding(kwRes.getEmbedding());
                    return keywordRepository.save(newKw); // 진짜 새로운 녀석만 INSERT
                });

                news.addKeyword(keyword, kwRes.getWeight());
            }
        }

        // 9. 뉴스 최종 업데이트
        newsRepository.saveAll(savedNews);
        log.info(">>>> [Batch Writer] Finished analyzing and summarizing {} news articles.", savedNews.size());

        // 10. 키워드 관계 저장
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