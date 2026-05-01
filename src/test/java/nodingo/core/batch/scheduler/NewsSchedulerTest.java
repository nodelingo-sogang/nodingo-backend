package nodingo.core.batch.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsSchedulerTest {

    @InjectMocks
    private NewsScheduler newsScheduler;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job dailyNewsJob;

    @Test
    void schedulerShouldPassCorrectJobParameters() throws Exception {
        // given
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);

        // when
        newsScheduler.runDailyNewsJob();

        // then
        verify(jobLauncher).run(eq(dailyNewsJob), captor.capture());

        JobParameters params = captor.getValue();

        assertThat(params.getParameters()).containsKey("requestTime");
        assertThat(params.getParameters()).containsKey("runId");
        assertThat(params.getParameters()).doesNotContainKey("unused");
    }
}