package nodingo.core.batch.integration;

import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.event.*;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Sql(
        scripts = "classpath:org/springframework/batch/core/schema-postgresql.sql",
        config = @SqlConfig(errorMode = SqlConfig.ErrorMode.CONTINUE_ON_ERROR)
)
class NewsBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dailyNewsJob")
    private Job dailyNewsJob;

    @MockitoBean
    private NewsFetchService newsFetchService;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(dailyNewsJob);

        jdbcTemplate.execute("""
            TRUNCATE TABLE
                news_keywords,
                keyword_alias,
                keywords,
                news
            RESTART IDENTITY CASCADE
        """);
    }

    @Test
    void jobShouldCompleteSuccessfully() throws Exception {
        // given
        EventApiItem eventItem = createMockEvent("event-uri-1", "article-uri-1");
        NewsApiItem fullArticle = createMockArticle("article-uri-1");

        EventWrapper wrapper = new EventWrapper();
        setField(wrapper, "results", List.of(eventItem));
        setField(wrapper, "pages", 1);

        EventApiResponse response = new EventApiResponse();
        setField(response, "events", wrapper);

        given(newsFetchService.fetchEvents(any(), anyInt()))
                .willReturn(response);

        given(newsFetchService.fetchArticle(eq("article-uri-1")))
                .willReturn(fullArticle);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("requestTime", LocalDateTime.now())
                .addString("runId", UUID.randomUUID().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(newsRepository.count()).isEqualTo(1);
        assertThat(keywordRepository.count()).isEqualTo(1);

        Map<String, Object> savedNews = jdbcTemplate.queryForMap("""
        SELECT uri, title, body, url, lang
        FROM news
        WHERE uri = ?
        """, "article-uri-1");

        assertThat(savedNews.get("uri")).isEqualTo("article-uri-1");
        assertThat(savedNews.get("title")).isEqualTo("Full Article Title");
        assertThat((String) savedNews.get("body")).contains("full article body");
        assertThat(savedNews.get("url")).isEqualTo("https://news.com/article-uri-1");
        assertThat(savedNews.get("lang")).isEqualTo("kor");

        Integer newsKeywordCount = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM news_keywords nk
        JOIN news n ON nk.news_id = n.id
        WHERE n.uri = ?
        """, Integer.class, "article-uri-1");

        assertThat(newsKeywordCount).isEqualTo(1);

        log.info(">>>> Saved News URI: {}", savedNews.get("uri"));
        log.info(">>>> Saved News Title: {}", savedNews.get("title"));
        log.info(">>>> Saved News Body: {}", savedNews.get("body"));
        log.info(">>>> Saved News Keywords Count: {}", newsKeywordCount);
    }

    private EventApiItem createMockEvent(String eventUri, String articleUri) {
        EventApiItem event = new EventApiItem();

        setField(event, "uri", eventUri);
        setField(event, "sentiment", 0.5);

        EventTitle title = new EventTitle();
        setField(title, "kor", "테스트 이벤트 제목");
        setField(title, "eng", "Test Event Title");
        setField(event, "title", title);

        Concept concept = new Concept();
        setField(concept, "type", "org");
        setField(concept, "score", 100);

        ConceptLabel label = new ConceptLabel();
        setField(label, "kor", "테슬라");
        setField(label, "eng", "Tesla");
        setField(concept, "label", label);

        setField(event, "concepts", List.of(concept));

        NewsApiItem infoArticle = new NewsApiItem();
        setField(infoArticle, "uri", articleUri);
        setField(infoArticle, "url", "https://news.com/" + articleUri);
        setField(infoArticle, "lang", "kor");

        InfoArticleWrapper articleWrapper = new InfoArticleWrapper();
        setField(articleWrapper, "kor", infoArticle);
        setField(articleWrapper, "eng", infoArticle);

        setField(event, "infoArticle", articleWrapper);

        return event;
    }

    private NewsApiItem createMockArticle(String articleUri) {
        NewsApiItem articleItem = new NewsApiItem();

        setField(articleItem, "uri", articleUri);
        setField(articleItem, "url", "https://news.com/" + articleUri);
        setField(articleItem, "lang", "kor");
        setField(articleItem, "dateTimePub", "2026-05-01T10:00:00Z");
        setField(articleItem, "title", "Full Article Title");
        setField(articleItem, "body", "This is the full article body content. This body should be saved into news table.");

        return articleItem;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}