package nodingo.core.batch.listener;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.batch.core.*;

class MyJobListenerTest {

    private JobExecution createJobExecution() {
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());
        return jobExecution;
    }

    @Test
    void beforeJob_shouldNotThrow() {
        MyJobListener listener = new MyJobListener();

        JobExecution jobExecution = createJobExecution();
        jobExecution.setStartTime(LocalDateTime.now());

        listener.beforeJob(jobExecution);

        assertThat(jobExecution).isNotNull();
    }

    @Test
    void afterJob_completed() {
        MyJobListener listener = new MyJobListener();

        JobExecution jobExecution = createJobExecution();
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.now().minusSeconds(5));
        jobExecution.setEndTime(LocalDateTime.now());

        listener.afterJob(jobExecution);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void afterJob_failed() {
        MyJobListener listener = new MyJobListener();

        JobExecution jobExecution = createJobExecution();
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setStartTime(LocalDateTime.now().minusSeconds(5));
        jobExecution.setEndTime(LocalDateTime.now());

        jobExecution.addFailureException(new RuntimeException("테스트 에러"));

        listener.afterJob(jobExecution);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(jobExecution.getAllFailureExceptions()).isNotEmpty();
    }
}