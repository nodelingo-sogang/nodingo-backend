package nodingo.core.batch.news.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.event.Concept;
import nodingo.core.batch.dto.event.EventApiItem;
import nodingo.core.batch.dto.event.NewsApiItem;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class NewsAiProcessor implements ItemProcessor<EventApiItem, News> {

    private final NewsRepository newsRepository;
    private final KeywordRepository keywordRepository;
    private final NewsFetchService newsFetchService;

    @Override
    public News process(EventApiItem eventItem) {
        if (eventItem == null) return null;

        // 1. URI 추출 및 검증
        String articleUri = extractArticleUri(eventItem);
        if (articleUri == null || articleUri.isBlank()) {
            log.warn(">>>> [Batch Processor] Skip: uri is empty. eventUri={}", eventItem.getUri());
            return null;
        }

        // 2. 이미 존재하는 뉴스인지 체크 (중복 수집 방지)
        if (newsRepository.existsByUri(articleUri)) {
            log.info(">>>> [Batch Processor] Skip: existing article. articleUri={}", articleUri);
            return null;
        }

        // 3. 뉴스 본문(Body) 상세 페치
        NewsApiItem articleItem;
        try {
            articleItem = newsFetchService.fetchArticle(articleUri);
        } catch (Exception e) {
            log.warn(">>>> [Batch Processor] Skip: fetch failed. articleUri={}, reason={}", articleUri, e.getMessage());
            return null;
        }

        if (articleItem == null || articleItem.getBody() == null || articleItem.getBody().isBlank()) {
            log.warn(">>>> [Batch Processor] Skip: body is empty. articleUri={}", articleUri);
            return null;
        }

        // 4. News 엔티티 생성
        News news = createNewsFromEventAndArticle(eventItem, articleItem);

        // 5. 키워드(Concepts) 매핑 로직
        if (eventItem.getConcepts() != null) {
            eventItem.getConcepts().stream()
                    .filter(this::isValidConcept)
                    .filter(concept -> concept.getScore() >= 70) // 점수 필터링
                    .forEach(concept -> {
                        String word = extractConceptWord(concept);
                        String normalized = word.toLowerCase().trim();

                        // 기존 키워드 조회 또는 신규 생성
                        Keyword keyword = keywordRepository.findByNormalizedWord(normalized)
                                .orElseGet(() -> keywordRepository.findByAlias(normalized)
                                        .orElseGet(() -> keywordRepository.save(Keyword.create(word))));

                        news.addKeyword(keyword, (double) concept.getScore());
                    });
        }

        return news;
    }

    private String extractArticleUri(EventApiItem eventItem) {
        if (eventItem.getInfoArticle() == null) {
            return null;
        }

        if (eventItem.getInfoArticle().getKor() != null
                && eventItem.getInfoArticle().getKor().getUri() != null
                && !eventItem.getInfoArticle().getKor().getUri().isBlank()) {
            return eventItem.getInfoArticle().getKor().getUri();
        }

        if (eventItem.getInfoArticle().getEng() != null
                && eventItem.getInfoArticle().getEng().getUri() != null
                && !eventItem.getInfoArticle().getEng().getUri().isBlank()) {
            return eventItem.getInfoArticle().getEng().getUri();
        }

        return null;
    }

    private News createNewsFromEventAndArticle(EventApiItem eventItem, NewsApiItem articleItem) {
        return News.create(
                articleItem.getUri(),
                resolveTitle(eventItem, articleItem),
                articleItem.getBody(),
                articleItem.getUrl(),
                articleItem.getLang(),
                resolveSentiment(eventItem),
                parseDateTimePub(articleItem.getDateTimePub())
        );
    }

    private String resolveTitle(EventApiItem eventItem, NewsApiItem articleItem) {
        if (articleItem.getTitle() != null && !articleItem.getTitle().isBlank()) {
            return articleItem.getTitle();
        }

        if (eventItem.getTitle() != null) {
            if (eventItem.getTitle().getKor() != null && !eventItem.getTitle().getKor().isBlank()) {
                return eventItem.getTitle().getKor();
            }

            if (eventItem.getTitle().getEng() != null && !eventItem.getTitle().getEng().isBlank()) {
                return eventItem.getTitle().getEng();
            }
        }

        return "Untitled News";
    }

    private Double resolveSentiment(EventApiItem eventItem) {
        return eventItem.getSentiment();
    }

    private LocalDateTime parseDateTimePub(String dateTimePub) {
        if (dateTimePub == null || dateTimePub.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return OffsetDateTime.parse(dateTimePub).toLocalDateTime();
        } catch (Exception e) {
            log.warn(">>>> [Batch Processor] Failed to parse dateTimePub: {}", dateTimePub);
            return LocalDateTime.now();
        }
    }

    private boolean isValidConcept(Concept concept) {
        String word = extractConceptWord(concept);
        return word != null && !word.isBlank();
    }

    private String extractConceptWord(Concept concept) {
        if (concept == null || concept.getLabel() == null) {
            return null;
        }

        if (concept.getLabel().getKor() != null && !concept.getLabel().getKor().isBlank()) {
            return concept.getLabel().getKor();
        }

        if (concept.getLabel().getEng() != null && !concept.getLabel().getEng().isBlank()) {
            return concept.getLabel().getEng();
        }

        return null;
    }
}