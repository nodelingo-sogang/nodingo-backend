package nodingo.core.batch.integration;

import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.keyword.KeywordSummary;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.ai.dto.relation.NewsRelationAnalysis;
import nodingo.core.batch.dto.article.ArticleWrapper;
import nodingo.core.batch.dto.article.NewsApiItem;
import nodingo.core.batch.dto.article.NewsApiResponse;
import nodingo.core.batch.service.query.NewsFetchService;
import nodingo.core.global.util.NewsSummarizer;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.keyword.service.command.KeywordRecommendService;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;

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

    @MockitoBean
    private KeywordRecommendService keywordRecommendService;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(dailyNewsJob);

        jdbcTemplate.execute("""
            TRUNCATE TABLE
                recommend_keywords,
                user_interests,
                user_personas,
                users,
                news_relations,
                keyword_relations,   
                news_keywords,
                keyword_alias,
                keywords,
                news
            RESTART IDENTITY CASCADE
        """);
    }

    @Test
    @DisplayName("뉴스 수집부터 AI 분석, 관계 맵핑, 유저 추천 및 AI 브리핑 요약까지 전체 배치가 성공적으로 수행된다")
    void jobShouldCompleteSuccessfully() throws Exception {
        // ==========================================
        // 1. Mocking: 외부 뉴스 API 응답 (뉴스 2개 세팅)
        // ==========================================
        NewsApiItem news1 = createMockArticle("uri-1", "테슬라 실적 발표", "테슬라의 1분기 실적이...");
        NewsApiItem news2 = createMockArticle("uri-2", "AI 반도체 시장", "엔비디아와 테슬라가 AI 반도체를...");

        NewsApiResponse response = new NewsApiResponse();
        ArticleWrapper wrapper = new ArticleWrapper();
        ReflectionTestUtils.setField(wrapper, "results", List.of(news1, news2));
        ReflectionTestUtils.setField(wrapper, "pages", 1);
        ReflectionTestUtils.setField(response, "articles", wrapper);

        given(newsFetchService.fetchNews(any(), anyInt())).willReturn(response);

        // ==========================================
        // 2. Mocking: AI 서버 - 뉴스 임베딩 & 키워드 분석 (Step 1)
        // ==========================================
        given(aiClient.analyzeNewsBatch(any(NewsBatch.Request.class)))
                .willAnswer(invocation -> {
                    NewsBatch.Request request = invocation.getArgument(0);

                    NewsBatch.KeywordAiResult kw1 = NewsBatch.KeywordAiResult.builder()
                            .keywordId(null).word("테슬라").normalizedWord("테슬라")
                            .weight(0.9).isNew(true).embedding(mockEmbedding()).build();

                    NewsBatch.KeywordAiResult kw2 = NewsBatch.KeywordAiResult.builder()
                            .keywordId(null).word("AI").normalizedWord("ai")
                            .weight(0.8).isNew(true).embedding(mockEmbedding()).build();

                    List<NewsBatch.NewsAnalysisResult> newsResults = request.getNews().stream()
                            .map(reqNews -> NewsBatch.NewsAnalysisResult.builder()
                                    .newsId(reqNews.getNewsId())
                                    .embedding(mockEmbedding())
                                    .keywords(List.of(kw1, kw2))
                                    .build())
                            .toList();

                    List<NewsBatch.KeywordRelationResult> kwRelations = List.of(
                            new NewsBatch.KeywordRelationResult(1L, 2L, 0.85)
                    );

                    return NewsBatch.Response.builder()
                            .newsResults(newsResults)
                            .keywordRelations(kwRelations)
                            .build();
                });

        given(newsSummarizer.summarize(any(News.class)))
                .willReturn("AI가 생성한 200자 요약 본문입니다.");

        // ==========================================
        // 3. Mocking: AI 서버 - 뉴스 간 관계 분석 (Step 2)
        // ==========================================
        NewsRelationAnalysis.RelationResult newsRelResult = new NewsRelationAnalysis.RelationResult(1L, 2L, 0.92);
        given(aiClient.buildNewsRelations(any(NewsRelationAnalysis.Request.class)))
                .willReturn(new NewsRelationAnalysis.Response(List.of(newsRelResult)));

        // ==========================================
        // 4. Data Setup & Mocking: 유저 추천 & AI 브리핑 (Step 3, 4)
        // ==========================================
        // UserReader가 읽어갈 대상 유저 생성 및 DB 저장
        User testUser = User.create("naver", "provider-123", "tester", "테스터", "test@test.com");
        userRepository.save(testUser);

        // Step 3: 추천 로직 우회 (빈 리스트 반환)
        given(keywordRecommendService.generateRecommendationForUser(any(), any(), any()))
                .willReturn(Collections.emptyList());

        // Step 4: AI 요약 로직 방어용 Mocking
        given(aiClient.summarizeKeywords(any(KeywordSummary.Request.class)))
                .willReturn(new KeywordSummary.Response(testUser.getId(), 1L, LocalDate.now(), "테스트용 AI 브리핑 요약 완료"));

        // ==========================================
        // 5. Execute Job
        // ==========================================
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("requestTime", LocalDateTime.now())
                .addString("runId", UUID.randomUUID().toString())
                .addString("targetDate", LocalDate.now().toString())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // ==========================================
        // 6. Verification
        // ==========================================
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(newsRepository.count()).isEqualTo(2);
        assertThat(keywordRepository.count()).isEqualTo(2);

        Integer newsKeywordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_keywords", Integer.class);
        assertThat(newsKeywordCount).isEqualTo(4);

        Integer kwRelCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM keyword_relations", Integer.class);
        assertThat(kwRelCount).isEqualTo(1);

        log.info(">>>> [Integration Test] Job Completed Successfully.");
        log.info(">>>> Saved News: 2, Saved Keywords: 2, Keyword Relations: {}", kwRelCount);
        log.info(">>>> 4단계 (뉴스수집 -> 관계맵핑 -> 유저추천 -> AI브리핑) 파이프라인 정상 구동 확인 완료.");
    }

    private NewsApiItem createMockArticle(String uri, String title, String body) {
        NewsApiItem articleItem = new NewsApiItem();
        ReflectionTestUtils.setField(articleItem, "uri", uri);
        ReflectionTestUtils.setField(articleItem, "url", "https://news.com/" + uri);
        ReflectionTestUtils.setField(articleItem, "lang", "kor");

        String yesterdaySafeTime = LocalDate.now().minusDays(1)
                .atTime(LocalTime.NOON)
                .atOffset(ZoneOffset.UTC)
                .toString();
        ReflectionTestUtils.setField(articleItem, "dateTimePub", yesterdaySafeTime);

        ReflectionTestUtils.setField(articleItem, "title", title);
        ReflectionTestUtils.setField(articleItem, "body", body);
        return articleItem;
    }

    private float[] mockEmbedding() {
        float[] embedding = new float[EMBEDDING_DIMENSION];
        embedding[0] = 0.1f;
        embedding[1] = 0.2f;
        embedding[2] = 0.3f;
        return embedding;
    }
}