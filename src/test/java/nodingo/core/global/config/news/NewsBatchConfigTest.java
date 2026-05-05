package nodingo.core.global.config.news;

import nodingo.core.batch.listener.MyJobListener;
import nodingo.core.batch.news.processor.NewsAiProcessor;
import nodingo.core.batch.news.reader.NewsApiReader;
import nodingo.core.batch.news.tasklet.NewsRelationTasklet;
import nodingo.core.batch.news.writer.NewsAiWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NewsBatchConfigTest {

    @Mock private JobRepository jobRepository;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private MyJobListener myJobListener;
    @Mock private NewsApiReader newsApiReader;
    @Mock private NewsAiProcessor newsProcessor;
    @Mock private NewsAiWriter newsAiWriter;
    @Mock private NewsRelationTasklet relationTasklet;

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
    @DisplayName("Job과 Step이 리팩토링된 타입(NewsApiItem) 기준으로 생성되어야 한다")
    void jobAndStepCreationTest() {
        // when
        Step newsStep = config.newsStep(newsApiReader, newsProcessor, newsAiWriter);
        Step relationStep = config.relationStep(relationTasklet);
        Job dailyJob = config.dailyNewsJob(newsStep, relationStep);

        // then
        assertThat(newsStep).isNotNull();
        assertThat(newsStep.getName()).isEqualTo("newsStep");
        assertThat(relationStep).isNotNull();
        assertThat(dailyJob).isNotNull();
        assertThat(dailyJob.getName()).isEqualTo("dailyNewsJob");
    }
}