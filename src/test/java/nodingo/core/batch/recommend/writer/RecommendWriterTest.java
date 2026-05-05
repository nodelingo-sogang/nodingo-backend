package nodingo.core.batch.recommend.writer;

import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendWriterTest {

    @Mock
    private RecommendKeywordRepository recommendKeywordRepository;

    @InjectMocks
    private RecommendWriter recommendWriterConfig;

    @Test
    @DisplayName("Chunk로 들어온 이중 리스트가 평탄화되어 saveAll을 호출해야 한다")
    void recommendWriterTest() throws Exception {
        // given
        ItemWriter<List<RecommendKeyword>> writer = recommendWriterConfig.recommendItemWriter();

        List<RecommendKeyword> user1Keywords = List.of(mock(RecommendKeyword.class), mock(RecommendKeyword.class));
        List<RecommendKeyword> user2Keywords = List.of(mock(RecommendKeyword.class));

        Chunk<List<RecommendKeyword>> chunk = new Chunk<>(List.of(user1Keywords, user2Keywords));

        // when
        writer.write(chunk);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RecommendKeyword>> captor = ArgumentCaptor.forClass(List.class);

        verify(recommendKeywordRepository, times(1)).saveAll(captor.capture());

        List<RecommendKeyword> savedKeywords = captor.getValue();
        assertThat(savedKeywords).hasSize(3);
    }

    @Test
    @DisplayName("추천 키워드가 빈 리스트일 경우 saveAll을 호출하지 않아야 한다")
    void recommendWriterEmptyTest() throws Exception {
        // given
        ItemWriter<List<RecommendKeyword>> writer = recommendWriterConfig.recommendItemWriter();
        Chunk<List<RecommendKeyword>> chunk = new Chunk<>(List.of(List.of(), List.of()));

        // when
        writer.write(chunk);

        // then
        verify(recommendKeywordRepository, never()).saveAll(any());
    }
}