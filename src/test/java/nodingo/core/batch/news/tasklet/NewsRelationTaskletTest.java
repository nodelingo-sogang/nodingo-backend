package nodingo.core.batch.news.tasklet;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.relation.NewsRelationAnalysis;
import nodingo.core.news.domain.News;
import nodingo.core.news.domain.NewsRelation;
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
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class NewsRelationTaskletTest {

    @Mock private NewsRepository newsRepository;
    @Mock private NewsRelationRepository newsRelationRepository;
    @Mock private AiClient aiClient;

    @InjectMocks private NewsRelationTasklet tasklet;

    @Mock private StepContribution stepContribution;
    @Mock private ChunkContext chunkContext;

    @Test
    @DisplayName("임베딩이 있는 뉴스가 2개 미만이면 AI 분석 없이 배치를 종료한다")
    void execute_NotEnoughNews() throws Exception {
        // given: create()로 생성 후, 테스트 조건을 위해 임베딩을 강제로 null로 덮어씌움
        News news1 = News.create(
                "uri1", "title1", "body1", "url1", "kor", 0.0, LocalDateTime.now()
        );
        ReflectionTestUtils.setField(news1, "embedding", null);

        given(newsRepository.findAll()).willReturn(List.of(news1));

        // when
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(aiClient, never()).buildNewsRelations(any());
        verify(newsRelationRepository, never()).save(any());
    }

    @Test
    @DisplayName("뉴스 임베딩을 모아 AI 서버에 전송하고, 반환된 관계 데이터를 DB에 저장한다")
    void execute_Success() throws Exception {
        // given: 정상적인 뉴스 2개 생성 및 ID/임베딩 강제 주입
        News news1 = News.create(
                "uri1", "title1", "body1", "url1", "kor", 0.0, LocalDateTime.now()
        );
        ReflectionTestUtils.setField(news1, "id", 1L);
        ReflectionTestUtils.setField(news1, "embedding", new float[]{0.1f, 0.2f});

        News news2 = News.create(
                "uri2", "title2", "body2", "url2", "kor", 0.0, LocalDateTime.now()
        );
        ReflectionTestUtils.setField(news2, "id", 2L);
        ReflectionTestUtils.setField(news2, "embedding", new float[]{0.3f, 0.4f});

        given(newsRepository.findAll()).willReturn(List.of(news1, news2));

        // AI 서버 응답 Mocking
        NewsRelationAnalysis.RelationResult aiResult =
                new NewsRelationAnalysis.RelationResult(1L, 2L, 0.85);
        NewsRelationAnalysis.Response aiResponse =
                new NewsRelationAnalysis.Response(List.of(aiResult));

        given(aiClient.buildNewsRelations(any(NewsRelationAnalysis.Request.class)))
                .willReturn(aiResponse);

        given(newsRepository.findById(1L)).willReturn(Optional.of(news1));
        given(newsRepository.findById(2L)).willReturn(Optional.of(news2));

        // when
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(aiClient).buildNewsRelations(any(NewsRelationAnalysis.Request.class));
        verify(newsRelationRepository, times(1)).save(any(NewsRelation.class));
    }
}