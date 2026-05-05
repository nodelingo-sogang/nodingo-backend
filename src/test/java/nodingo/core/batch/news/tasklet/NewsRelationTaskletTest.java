package nodingo.core.batch.news.tasklet;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.relation.NewsRelationAnalysis;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRelationRepository;
import nodingo.core.news.repository.NewsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class NewsRelationTaskletTest {

    @Mock private NewsRepository newsRepository;
    @Mock private NewsRelationRepository newsRelationRepository;
    @Mock private AiClient aiClient;

    @InjectMocks private NewsRelationTasklet tasklet;

    @Mock private StepContribution stepContribution;
    @Mock private ChunkContext chunkContext;

    @Test
    @DisplayName("임베딩이 있는 뉴스가 오늘 수집된 범위 내에 2개 미만이면 종료한다")
    void execute_NotEnoughNews() throws Exception {
        // given
        News news1 = News.create("uri1", "title1", "body1", "url1", "kor", 0.0, LocalDateTime.now());
        ReflectionTestUtils.setField(news1, "embedding", null);

        given(newsRepository.findAllByDateTimePubBetweenAndEmbeddingIsNotNull(any(), any()))
                .willReturn(List.of());

        // when
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(aiClient, never()).buildNewsRelations(any());
    }

    @Test
    @DisplayName("당일 뉴스를 AI 서버로 보내 유사도를 분석하고 결과를 Bulk Save한다")
    void execute_Success() throws Exception {
        // given
        News news1 = News.create("uri1", "title1", "body1", "url1", "kor", 0.0, LocalDateTime.now());
        ReflectionTestUtils.setField(news1, "id", 1L);
        ReflectionTestUtils.setField(news1, "embedding", new float[]{0.1f});

        News news2 = News.create("uri2", "title2", "body2", "url2", "kor", 0.0, LocalDateTime.now());
        ReflectionTestUtils.setField(news2, "id", 2L);
        ReflectionTestUtils.setField(news2, "embedding", new float[]{0.3f});

        given(newsRepository.findAllByDateTimePubBetweenAndEmbeddingIsNotNull(any(), any()))
                .willReturn(List.of(news1, news2));

        NewsRelationAnalysis.RelationResult aiResult = new NewsRelationAnalysis.RelationResult(1L, 2L, 0.85);
        NewsRelationAnalysis.Response aiResponse = new NewsRelationAnalysis.Response(List.of(aiResult));

        given(aiClient.buildNewsRelations(any(NewsRelationAnalysis.Request.class))).willReturn(aiResponse);

        // when
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(aiClient).buildNewsRelations(any());

        verify(newsRelationRepository).saveAll(anyList());
    }
}