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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.Chunk;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class NewsBatchConfigTest {

    @Mock private JobRepository jobRepository;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private MyJobListener myJobListener;

    // 부품들 Mock
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
                myJobListener,
                newsApiReader,
                newsProcessor,
                newsAiWriter
        );
    }

    @Test
    @DisplayName("Job과 Step이 설정대로 생성되어야 한다")
    void jobAndStepCreationTest() {
        // when
        Step newsStep = config.newsStep(newsApiReader, newsProcessor, newsAiWriter);
        Step relationStep = config.relationStep(relationTasklet);
        Job dailyJob = config.dailyNewsJob(newsStep, relationStep);

        // then
        assertThat(newsStep).isNotNull();
        assertThat(newsStep.getName()).isEqualTo("newsStep");
        assertThat(dailyJob).isNotNull();
        assertThat(dailyJob.getName()).isEqualTo("dailyNewsJob");
    }
}