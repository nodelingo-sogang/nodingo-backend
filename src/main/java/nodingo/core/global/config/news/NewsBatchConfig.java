package nodingo.core.global.config.news;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.event.Concept;
import nodingo.core.batch.dto.event.EventApiItem;
import nodingo.core.batch.dto.event.EventApiResponse;
import nodingo.core.batch.dto.event.NewsApiItem;
import nodingo.core.batch.listener.MyJobListener;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NewsBatchConfig {

    private static final int CHUNK_SIZE = 20;
    private static final int MAX_TEST_PAGES = 20; // Get Events는 페이지당 50개니 최대 500개

    private final NewsFetchService newsFetchService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NewsRepository newsRepository;
    private final KeywordRepository keywordRepository;
    private final MyJobListener myJobListener;

    @Bean
    public Job dailyNewsJob() {
        return new JobBuilder("dailyNewsJob", jobRepository)
                .listener(myJobListener)
                .start(newsStep())
                .build();
    }

    @Bean
    public Step newsStep() {
        return new StepBuilder("newsStep", jobRepository)
                .<EventApiItem, News>chunk(CHUNK_SIZE, transactionManager)
                .reader(newsReader())
                .processor(newsProcessor())
                .writer(newsWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<EventApiItem> newsReader() {
        return new ItemReader<>() {

            private int currentPage = 1;
            private Iterator<EventApiItem> itemIterator = Collections.emptyIterator();
            private boolean isEnd = false;

            @Override
            public EventApiItem read() {

                if (itemIterator.hasNext()) {
                    return itemIterator.next();
                }

                if (isEnd || currentPage > MAX_TEST_PAGES) {
                    log.info(">>>> [Batch Reader] Finished or reached max test pages.");
                    return null;
                }

                LocalDate targetDate = LocalDate.now().minusDays(1);

                EventApiResponse response = newsFetchService.fetchEvents(targetDate, currentPage);

                if (response == null
                        || response.getEvents() == null
                        || response.getEvents().getResults() == null
                        || response.getEvents().getResults().isEmpty()) {

                    log.warn(">>>> [Batch Reader] API response is empty. date: {}, page: {}",
                            targetDate, currentPage);
                    isEnd = true;
                    return null;
                }

                List<EventApiItem> results = response.getEvents().getResults();
                itemIterator = results.iterator();

                int totalPages = response.getEvents().getPages() > 0
                        ? response.getEvents().getPages()
                        : currentPage;

                int effectiveTotalPages = Math.min(totalPages, MAX_TEST_PAGES);

                log.info(">>>> [Batch Reader] Fetching Event page: {} / {} (total={}, items={})",
                        currentPage, effectiveTotalPages, totalPages, results.size());

                if (currentPage >= effectiveTotalPages) {
                    isEnd = true;
                }

                currentPage++;

                return itemIterator.hasNext() ? itemIterator.next() : null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<EventApiItem, News> newsProcessor() {
        return eventItem -> {
            if (eventItem == null) {
                return null;
            }

            String articleUri = extractArticleUri(eventItem);

            if (articleUri == null || articleUri.isBlank()) {
                log.warn(">>>> [Batch Processor] Skip event. infoArticle.uri is empty. eventUri={}",
                        eventItem.getUri());
                return null;
            }

            NewsApiItem articleItem;

            if (newsRepository.existsByUri(articleUri)) {
                log.info(">>>> [Batch Processor] Skip existing article. articleUri={}", articleUri);
                return null;
            }

            try {
                articleItem = newsFetchService.fetchArticle(articleUri);
            } catch (Exception e) {
                log.warn(">>>> [Batch Processor] Skip event. Failed to fetch article body. eventUri={}, articleUri={}, reason={}",
                        eventItem.getUri(), articleUri, e.getMessage());
                return null;
            }

            if (articleItem == null || articleItem.getBody() == null || articleItem.getBody().isBlank()) {
                log.warn(">>>> [Batch Processor] Skip event. Article body is empty. eventUri={}, articleUri={}",
                        eventItem.getUri(), articleUri);
                return null;
            }

            News news = createNewsFromEventAndArticle(eventItem, articleItem);

            if (eventItem.getConcepts() != null) {
                eventItem.getConcepts().stream()
                        .filter(this::isValidConcept)
                        .filter(concept -> concept.getScore() >= 70)
                        .forEach(concept -> {
                            String word = extractConceptWord(concept);
                            String normalized = word.toLowerCase().trim();

                            Keyword keyword = keywordRepository.findByNormalizedWord(normalized)
                                    .orElseGet(() -> keywordRepository.findByAlias(normalized)
                                            .orElseGet(() -> keywordRepository.save(Keyword.create(word))));

                            news.addKeyword(keyword, (double) concept.getScore());
                        });
            }
            return news;
        };
    }

    @Bean
    public ItemWriter<News> newsWriter() {
        return items -> {
            List<? extends News> chunkItems = items.getItems();

            List<News> distinctItems = new ArrayList<>(chunkItems.stream()
                    .collect(Collectors.toMap(
                            News::getUri,
                            news -> news,
                            (existing, replacement) -> existing
                    ))
                    .values());

            List<String> uris = distinctItems.stream()
                    .map(News::getUri)
                    .toList();

            Set<String> existingUris = new HashSet<>(newsRepository.findExistingUris(uris));

            List<News> toSave = distinctItems.stream()
                    .filter(news -> !existingUris.contains(news.getUri()))
                    .toList();

            if (!toSave.isEmpty()) {
                newsRepository.saveAll(toSave);
            }

            log.info(">>>> [Batch Writer] processed={} | saved={} | skipped={}",
                    items.size(), toSave.size(), items.size() - toSave.size());
        };
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