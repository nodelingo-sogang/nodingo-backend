package nodingo.core.global.config.news;

import com.google.firebase.messaging.Message;
import nodingo.core.batch.dto.article.NewsApiItem;
import nodingo.core.batch.listener.MyJobListener;

import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.news.domain.News;
import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


@ExtendWith(MockitoExtension.class)
class NewsBatchConfigTest {

    @Mock private JobRepository jobRepository;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private MyJobListener myJobListener;

    // Step 1: News 수집 관련
    @Mock private ItemReader<NewsApiItem> newsApiReader;
    @Mock private ItemProcessor<NewsApiItem, News> newsProcessor;
    @Mock private ItemWriter<News> newsAiWriter;

    // Step 2: 관계 추출 관련
    @Mock private Tasklet relationTasklet;

    // Step 3: 유저 추천 관련
    @Mock private ItemReader<User> userReader;
    @Mock private ItemProcessor<User, List<RecommendKeyword>> recommendProcessor;
    @Mock private ItemWriter<List<RecommendKeyword>> recommendWriter;

    // Step 4: 요약 생성 관련
    @Mock private ItemReader<RecommendKeyword> recommendSummaryReader;
    @Mock private ItemProcessor<RecommendKeyword, RecommendKeyword> recommendSummaryProcessor;
    @Mock private ItemWriter<RecommendKeyword> recommendSummaryWriter;

    // Step 5: 알림 발송 관련
    @Mock private ItemReader<NotificationSetting> notificationReader;
    @Mock private ItemProcessor<NotificationSetting, Message> notificationProcessor;
    @Mock private ItemWriter<Message> fcmBatchWriter;

    private NewsBatchConfig config;

    @BeforeEach
    void setUp() {
        config = new NewsBatchConfig(
                jobRepository,
                transactionManager,
                myJobListener
        );
    }

    @Test
    @DisplayName("Job과 5개의 Step이 정상 생성되고 순서대로 연결된다")
    void jobAndStepCreationTest() {

        // when
        Step newsStep = config.newsStep(newsApiReader, newsProcessor, newsAiWriter);
        Step relationStep = config.relationStep(relationTasklet);
        Step recommendStep = config.recommendStep(userReader, recommendProcessor, recommendWriter);
        Step summaryStep = config.recommendSummaryStep(
                recommendSummaryReader,
                recommendSummaryProcessor,
                recommendSummaryWriter
        );
        Step notificationStep = config.notificationStep(
                notificationReader,
                notificationProcessor,
                fcmBatchWriter
        );

        Job job = config.dailyNewsJob(newsStep, relationStep, recommendStep, summaryStep, notificationStep);

        // then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("dailyNewsJob");

        SimpleJob simpleJob = (SimpleJob) job;

        assertThat(simpleJob.getStepNames()).containsExactly(
                "newsStep",
                "relationStep",
                "recommendStep",
                "recommendSummaryStep",
                "notificationStep"
        );
    }
}