package nodingo.core.batch.integration;

import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.batch.dto.event.*;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.global.util.NewsSummarizer;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
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
import org.springframework.test.util.ReflectionTestUtils;

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

    private static final int EMBEDDING_DIMENSION = 1024;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dailyNewsJob")
    private Job dailyNewsJob;

    @MockitoBean
    private NewsFetchService newsFetchService;

    @MockitoBean
    private AiClient aiClient;

    @MockitoBean
    private NewsSummarizer newsSummarizer;

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
                news_relations,   
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

        given(aiClient.analyzeNewsBatch(any(NewsBatch.Request.class)))
                .willAnswer(invocation -> {
                    NewsBatch.Request request = invocation.getArgument(0);

                    Long newsId = request.getNews().get(0).getNewsId();

                    NewsBatch.KeywordAiResult keywordResult = NewsBatch.KeywordAiResult.builder()
                            .keywordId(null)
                            .word("인공지능")
                            .weight(0.9)
                            .isNew(true)
                            .embedding(mockEmbedding())
                            .build();

                    NewsBatch.NewsAnalysisResult newsResult = NewsBatch.NewsAnalysisResult.builder()
                            .newsId(newsId)
                            .embedding(mockEmbedding())
                            .keywords(List.of(keywordResult))
                            .build();

                    return NewsBatch.Response.builder()
                            .newsResults(List.of(newsResult))
                            .build();
                });

        given(newsSummarizer.summarize(any(News.class)))
                .willReturn("AI가 생성한 200자 요약 본문입니다.");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("requestTime", LocalDateTime.now())
                .addString("runId", UUID.randomUUID().toString())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(newsRepository.count()).isEqualTo(1);
        assertThat(keywordRepository.count()).isEqualTo(2);

        List<String> keywords = jdbcTemplate.queryForList("""
        SELECT word
        FROM keywords
        """, String.class);

        assertThat(keywords).contains("테슬라", "인공지능");

        Map<String, Object> savedNews = jdbcTemplate.queryForMap("""
            SELECT uri, title, body, url, lang
            FROM news
            WHERE uri = ?
            """, "article-uri-1");

        assertThat(savedNews.get("uri")).isEqualTo("article-uri-1");
        assertThat(savedNews.get("title")).isEqualTo("Full Article Title");

        // NewsSummarizer mock 결과가 저장되는 구조이므로 요약문 기준으로 검증
        assertThat(savedNews.get("body")).isEqualTo("AI가 생성한 200자 요약 본문입니다.");

        assertThat(savedNews.get("url")).isEqualTo("https://news.com/article-uri-1");
        assertThat(savedNews.get("lang")).isEqualTo("kor");

        Integer newsKeywordCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM news_keywords nk
            JOIN news n ON nk.news_id = n.id
            WHERE n.uri = ?
            """, Integer.class, "article-uri-1");

        assertThat(newsKeywordCount).isEqualTo(2);

        log.info(">>>> Saved News URI: {}", savedNews.get("uri"));
        log.info(">>>> Saved News Title: {}", savedNews.get("title"));
        log.info(">>>> Saved News Body: {}", savedNews.get("body"));
        log.info(">>>> Saved News Keywords Count: {}", newsKeywordCount);
    }

    private EventApiItem createMockEvent(String eventUri, String articleUri) {
        EventApiItem event = new EventApiItem();

        ReflectionTestUtils.setField(event, "uri", eventUri);
        ReflectionTestUtils.setField(event, "sentiment", 0.5);

        EventTitle title = new EventTitle();
        ReflectionTestUtils.setField(title, "kor", "테스트 이벤트 제목");
        ReflectionTestUtils.setField(title, "eng", "Test Event Title");
        ReflectionTestUtils.setField(event, "title", title);

        Concept concept = new Concept();
        ReflectionTestUtils.setField(concept, "type", "org");
        ReflectionTestUtils.setField(concept, "score", 100);

        ConceptLabel label = new ConceptLabel();
        ReflectionTestUtils.setField(label, "kor", "테슬라");
        ReflectionTestUtils.setField(label, "eng", "Tesla");
        ReflectionTestUtils.setField(concept, "label", label);

        ReflectionTestUtils.setField(event, "concepts", List.of(concept));

        NewsApiItem infoArticle = new NewsApiItem();
        ReflectionTestUtils.setField(infoArticle, "uri", articleUri);
        ReflectionTestUtils.setField(infoArticle, "url", "https://news.com/" + articleUri);
        ReflectionTestUtils.setField(infoArticle, "lang", "kor");

        InfoArticleWrapper articleWrapper = new InfoArticleWrapper();
        ReflectionTestUtils.setField(articleWrapper, "kor", infoArticle);
        ReflectionTestUtils.setField(articleWrapper, "eng", infoArticle);

        ReflectionTestUtils.setField(event, "infoArticle", articleWrapper);

        return event;
    }

    private NewsApiItem createMockArticle(String articleUri) {
        NewsApiItem articleItem = new NewsApiItem();

        ReflectionTestUtils.setField(articleItem, "uri", articleUri);
        ReflectionTestUtils.setField(articleItem, "url", "https://news.com/" + articleUri);
        ReflectionTestUtils.setField(articleItem, "lang", "kor");
        ReflectionTestUtils.setField(articleItem, "dateTimePub", "2026-05-01T10:00:00Z");
        ReflectionTestUtils.setField(articleItem, "title", "Full Article Title");
        ReflectionTestUtils.setField(articleItem, "body", "This is the full article body content. This body should be saved into news table.");

        return articleItem;
    }

    private float[] mockEmbedding() {
        float[] embedding = new float[EMBEDDING_DIMENSION];
        embedding[0] = 0.1f;
        embedding[1] = 0.2f;
        embedding[2] = 0.3f;
        return embedding;
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