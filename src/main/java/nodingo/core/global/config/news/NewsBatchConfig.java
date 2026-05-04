package nodingo.core.global.config.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.event.EventApiItem;
import nodingo.core.batch.listener.MyJobListener;
import nodingo.core.batch.news.processor.NewsAiProcessor;
import nodingo.core.batch.news.reader.NewsApiReader;
import nodingo.core.batch.news.writer.NewsAiWriter;
import nodingo.core.batch.news.tasklet.NewsRelationTasklet;
import nodingo.core.news.domain.News;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NewsBatchConfig {

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MyJobListener myJobListener;
    private final NewsApiReader newsApiReader;
    private final NewsAiProcessor newsProcessor;
    private final NewsAiWriter newsAiWriter;

    @Bean
    public Job dailyNewsJob(Step newsStep, Step relationStep) {
        return new JobBuilder("dailyNewsJob", jobRepository)
                .listener(myJobListener)
                .start(newsStep)
                .next(relationStep)
                .build();
    }

    @Bean
    public Step newsStep(ItemReader<EventApiItem> newsApiReader,  // 인터페이스로 변경
                         ItemProcessor<EventApiItem, News> newsProcessor, // 인터페이스로 변경
                         ItemWriter<News> newsAiWriter) { // 인터페이스로 변경
        return new StepBuilder("newsStep", jobRepository)
                .<EventApiItem, News>chunk(CHUNK_SIZE, transactionManager)
                .reader(newsApiReader)
                .processor(newsProcessor)
                .writer(newsAiWriter)
                .build();
    }

    @Bean
    public Step relationStep(NewsRelationTasklet relationTasklet) {
        return new StepBuilder("relationStep", jobRepository)
                .tasklet(relationTasklet, transactionManager)
                .build();
    }
}