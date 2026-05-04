package nodingo.core.batch.news.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.global.util.NewsSummarizer;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class NewsAiWriter implements ItemWriter<News> {

    private final NewsRepository newsRepository;
    private final KeywordRepository keywordRepository;
    private final AiClient aiClient;
    private final NewsSummarizer newsSummarizer;

    private static final int TOP_K_KEYWORDS = 5;

    @Override
    public void write(Chunk<? extends News> items) {
        List<News> chunkItems = new ArrayList<>(items.getItems());

        if (chunkItems.isEmpty()) return;

        // 1. DB 1차 저장 (AI 서버에 보낼 ID 확보)
        List<News> savedNews = newsRepository.saveAll(chunkItems);

        // 2. 기존 키워드 목록 조회 (AI 서버의 분석 가이드용)
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

        log.info(">>>> [Batch Writer] {}건 뉴스 AI 분석 요청 시작...", savedNews.size());

        // 4. FastAPI 서버 호출
        NewsBatch.Response aiResponse = aiClient.analyzeNewsBatch(request);

        // 5. AI 분석 결과 반영 루프
        for (NewsBatch.NewsAnalysisResult res : aiResponse.getNewsResults()) {
            News news = savedNews.stream()
                    .filter(n -> n.getId().equals(res.getNewsId()))
                    .findFirst()
                    .orElse(null);

            if (news == null) continue;

            // 5-1. 임베딩 업데이트
            news.updateEmbedding(res.getEmbedding());

            // 5-2. LLM 요약 수행 (본문 200자 내외)
            String summary = newsSummarizer.summarize(news);
            news.updateBody(summary);

            // 5-3. 키워드 처리 (신규 생성 or 기존 연결)
            for (NewsBatch.KeywordAiResult kwRes : res.getKeywords()) {
                Keyword keyword;

                if (kwRes.isNew()) {
                    // 신규 키워드인 경우
                    keyword = Keyword.create(kwRes.getWord());
                    keyword.updateEmbedding(kwRes.getEmbedding());
                    keyword = keywordRepository.save(keyword);
                } else {
                    // 기존 키워드인 경우
                    keyword = keywordRepository.findById(kwRes.getKeywordId())
                            .orElseGet(() -> keywordRepository.save(Keyword.create(kwRes.getWord())));
                }

                news.addKeyword(keyword, kwRes.getWeight());
            }
        }

        // 6. 최종 변경 사항 DB 반영 (더티 체킹보다 안전한 saveAll)
        newsRepository.saveAll(savedNews);

        log.info(">>>> [Batch Writer] finished {} of news analyzation and summarization", savedNews.size());
    }
}
