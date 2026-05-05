package nodingo.core.global.config.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.article.NewsApiItem;
import nodingo.core.batch.listener.MyJobListener;
import nodingo.core.news.domain.News;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
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

    @Bean
    public Job dailyNewsJob(Step newsStep, Step relationStep) {
        return new JobBuilder("dailyNewsJob", jobRepository)
                .listener(myJobListener)
                .start(newsStep)
                .next(relationStep)
                .build();
    }

    @Bean
    public Step newsStep(ItemReader<NewsApiItem> newsApiReader,
                         ItemProcessor<NewsApiItem, News> newsProcessor,
                         ItemWriter<News> newsAiWriter) {

        return new StepBuilder("newsStep", jobRepository)
                .<NewsApiItem, News>chunk(CHUNK_SIZE, transactionManager)
                .reader(newsApiReader)
                .processor(newsProcessor)
                .writer(newsAiWriter)
                .build();
    }

    @Bean
    public Step relationStep(Tasklet newsRelationTasklet) {
        return new StepBuilder("relationStep", jobRepository)
                .tasklet(newsRelationTasklet, transactionManager)
                .build();
    }
}