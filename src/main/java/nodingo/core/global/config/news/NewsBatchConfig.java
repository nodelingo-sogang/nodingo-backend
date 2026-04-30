package nodingo.core.global.config.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.NewsApiItem;
import nodingo.core.batch.dto.NewsApiResponse;
import nodingo.core.batch.listener.MyJobListener;
import nodingo.core.batch.service.NewsFetchService;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NewsBatchConfig {

    private static final int CHUNK_SIZE = 20;

    // 테스트/개발용 제한
    // articlesCount=100 기준: 10 pages = 최대 1000개
    private static final int MAX_TEST_PAGES = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NewsRepository newsRepository;
    private final NewsFetchService newsFetchService;
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
                .<NewsApiItem, News>chunk(CHUNK_SIZE, transactionManager)
                .reader(newsReader())
                .processor(newsProcessor())
                .writer(newsWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<NewsApiItem> newsReader() {
        return new ItemReader<>() {

            private int currentPage = 1;
            private Iterator<NewsApiItem> itemIterator = Collections.emptyIterator();
            private boolean isEnd = false;

            @Override
            public NewsApiItem read() {

                if (itemIterator.hasNext()) {
                    return itemIterator.next();
                }

                if (isEnd) {
                    log.info(">>>> [Batch Reader] Completed collecting news data.");
                    return null;
                }

                // 테스트/개발용 강제 종료 조건
                // 여기서 return null 하면 Step이 정상 종료됨
                if (currentPage > MAX_TEST_PAGES) {
                    log.info(">>>> [Batch Reader] Test page limit reached. maxPages={}, estimatedMaxArticles={}",
                            MAX_TEST_PAGES, MAX_TEST_PAGES * 100);
                    isEnd = true;
                    return null;
                }

                LocalDate targetDate = LocalDate.now().minusDays(1);

                NewsApiResponse response = newsFetchService.fetchNews(targetDate, currentPage, null);

                if (response == null
                        || response.getArticles() == null
                        || response.getArticles().getResults() == null
                        || response.getArticles().getResults().isEmpty()) {

                    log.warn(">>>> [Batch Reader] API response is empty. date: {}, page: {}",
                            targetDate, currentPage);

                    isEnd = true;
                    return null;
                }

                List<NewsApiItem> results = response.getArticles().getResults();
                itemIterator = results.iterator();

                int totalPages = response.getArticles().getPages();
                int effectiveTotalPages = Math.min(totalPages, MAX_TEST_PAGES);

                log.info(">>>> [Batch Reader] Fetching API page: {} / {} (apiTotalPages={}, testMaxPages={}, articles={})",
                        currentPage, effectiveTotalPages, totalPages, MAX_TEST_PAGES, results.size());

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
    public ItemProcessor<NewsApiItem, News> newsProcessor() {
        return NewsApiItem::toEntity;
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

            log.info(">>>> [Batch Writer] fetched={} | distinct={} | saved={} | skipped={}",
                    items.size(),
                    distinctItems.size(),
                    toSave.size(),
                    items.size() - toSave.size());
        };
    }
}