package nodingo.core.batch.integration;

import nodingo.core.batch.dto.ArticleWrapper;
import nodingo.core.batch.dto.NewsApiItem;
import nodingo.core.batch.dto.NewsApiResponse;
import nodingo.core.batch.service.NewsFetchService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;

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
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(dailyNewsJob);

        // News 관련 테이블만 초기화
        jdbcTemplate.execute("TRUNCATE TABLE news RESTART IDENTITY CASCADE");
    }

    @Test
    void jobShouldCompleteSuccessfully() throws Exception {

        // given
        NewsApiItem item = createItem("uri1");

        ArticleWrapper wrapper = new ArticleWrapper();
        setField(wrapper, "results", List.of(item));
        setField(wrapper, "pages", 1);

        NewsApiResponse response = new NewsApiResponse();
        setField(response, "articles", wrapper);

        given(newsFetchService.fetchNews(any(), anyInt(), nullable(String.class)))
                .willReturn(response);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 실패 원인 출력용
        jobExecution.getAllFailureExceptions()
                .forEach(Throwable::printStackTrace);

        // then
        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(newsRepository.count())
                .isGreaterThan(0);
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

    private NewsApiItem createItem(String uri) {
        NewsApiItem item = new NewsApiItem();

        setField(item, "uri", uri);
        setField(item, "title", "title");
        setField(item, "body", "body");
        setField(item, "url", "https://example.com/news/" + uri);
        setField(item, "lang", "eng");
        setField(item, "sentiment", 0.1);
        setField(item, "dateTimePub", "2024-01-01T10:00:00");

        return item;
    }
}