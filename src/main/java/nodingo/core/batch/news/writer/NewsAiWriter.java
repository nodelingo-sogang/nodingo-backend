package nodingo.core.batch.news.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
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
    private final KeywordRelationRepository keywordRelationRepository; // 🚀 새로 추가! (키워드 관계 저장용)
    private final AiClient aiClient;
    private final NewsSummarizer newsSummarizer;

    private static final int TOP_K_KEYWORDS = 5;

    @Override
    public void write(Chunk<? extends News> items) {
        List<News> chunkItems = new ArrayList<>(items.getItems());

        if (chunkItems.isEmpty()) return;

        // 1. DB 1차 저장 (AI 서버에 보낼 ID 확보)
        List<News> savedNews = newsRepository.saveAll(chunkItems);

        // 🚀 성능 최적화: N^2 탐색 방지를 위해 ID를 Key로 하는 Map 생성
        Map<Long, News> newsMap = savedNews.stream()
                .collect(Collectors.toMap(News::getId, Function.identity()));

        // 2. 기존 키워드 목록 조회
        // ⚠️ 강력 경고: 나중에 DB에 키워드가 수만 개 쌓이면 Chunk마다 findAll() 하는 건 엄청난 부담임!
        // TODO: Redis에 캐싱하거나, @Cacheable을 써서 하루에 한 번만 로드하도록 최적화 필수!
        List<NewsBatch.ExistingKeywordInput> existingKeywords = keywordRepository.findAll().stream()
                .map(k -> NewsBatch.ExistingKeywordInput.builder()
                        .keywordId(k.getId())
                        .word(k.getWord())
                        .normalizedWord(k.getNormalizedWord())
                        .embedding(k.getEmbedding())
                        .build())
                .toList();

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

        log.info(">>>> [Batch Writer] Requesting AI analysis for {} news articles...", savedNews.size());

        // 4. FastAPI 서버 호출
        NewsBatch.Response aiResponse = aiClient.analyzeNewsBatch(request);

        // 🚀 동일한 Chunk 내에서 새로 생성된 키워드 중복 저장 방지용 로컬 캐시
        Map<String, Keyword> newKeywordCache = new HashMap<>();

        // 5. AI 분석 결과 반영 루프
        for (NewsBatch.NewsAnalysisResult res : aiResponse.getNewsResults()) {

            // 🚀 리스트 필터링(O(N)) 대신 Map에서 한 방에 꺼내기(O(1))
            News news = newsMap.get(res.getNewsId());
            if (news == null) continue;

            // 5-1. 임베딩 업데이트
            news.updateEmbedding(res.getEmbedding());

            // 5-2. LLM 요약 수행 (본문 200자 내외)
            String summary = newsSummarizer.summarize(news);
            news.updateBody(summary);

            // 5-3. 키워드 처리
            for (NewsBatch.KeywordAiResult kwRes : res.getKeywords()) {
                Keyword keyword;

                if (kwRes.isNew()) {
                    // 신규 키워드인데 이번 Chunk에서 이미 만든 적이 있는지 캐시 확인
                    keyword = newKeywordCache.get(kwRes.getNormalizedWord());

                    if (keyword == null) {
                        keyword = Keyword.create(kwRes.getWord());
                        keyword.updateEmbedding(kwRes.getEmbedding());
                        keyword = keywordRepository.save(keyword);
                        newKeywordCache.put(kwRes.getNormalizedWord(), keyword); // 캐시에 등록!
                    }
                } else {
                    // 기존 키워드인 경우
                    keyword = keywordRepository.findById(kwRes.getKeywordId())
                            .orElseGet(() -> keywordRepository.save(Keyword.create(kwRes.getWord())));
                }

                // 뉴스-키워드 관계 매핑
                news.addKeyword(keyword, kwRes.getWeight());
            }
        }

        // 6. 뉴스 최종 변경 사항 DB 반영
        newsRepository.saveAll(savedNews);
        log.info(">>>> [Batch Writer] Finished analyzing and summarizing {} news articles.", savedNews.size());

        // 7.  키워드 간의 관계(Keyword Relation) DB 반영
        if (aiResponse.getKeywordRelations() != null && !aiResponse.getKeywordRelations().isEmpty()) {
            List<KeywordRelation> relationsToSave = new ArrayList<>();

            for (NewsBatch.KeywordRelationResult relRes : aiResponse.getKeywordRelations()) {
                // 키워드 ID가 둘 다 존재하는 확실한 관계만 저장
                if (relRes.getSourceKeywordId() != null && relRes.getTargetKeywordId() != null) {

                    Keyword source = keywordRepository.findById(relRes.getSourceKeywordId()).orElse(null);
                    Keyword target = keywordRepository.findById(relRes.getTargetKeywordId()).orElse(null);

                    if (source != null && target != null) {
                        KeywordRelation relation = KeywordRelation.create(source, target, relRes.getRelationScore());
                        relationsToSave.add(relation);
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