package nodingo.core.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailyNewsJob;

    @Scheduled(cron = "0 0 5 * * *")
    public void runDailyNewsJob() {
        log.info(">>>> [Scheduler] Starting news collection batch at 5 AM.");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("requestDate", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(dailyNewsJob, jobParameters);

        } catch (Exception e) {
            log.error(">>>> [Scheduler] Exception occurred during batch execution: {}", e.getMessage());
        }
    }
}
