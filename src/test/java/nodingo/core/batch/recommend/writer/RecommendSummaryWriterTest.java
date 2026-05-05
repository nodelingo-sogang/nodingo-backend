package nodingo.core.batch.recommend.writer;

import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendSummaryWriterTest {

    @InjectMocks
    private RecommendSummaryWriter writerConfig;

    @Mock
    private RecommendKeywordRepository recommendKeywordRepository;

    private ItemWriter<RecommendKeyword> writer;

    @BeforeEach
    void setUp() {
        writer = writerConfig.recommendSummaryItemWriter();
    }

    @Test
    @DisplayName("데이터 있으면 saveAll 호출")
    void saveAllCalled() throws Exception {
        RecommendKeyword rk = mock(RecommendKeyword.class);

        Chunk<RecommendKeyword> chunk = new Chunk<>(List.of(rk));

        writer.write(chunk);

        verify(recommendKeywordRepository).saveAll(any());
    }

    @Test
    @DisplayName("비어있으면 저장 안함")
    void skipWhenEmpty() throws Exception {
        Chunk<RecommendKeyword> chunk = new Chunk<>(List.of());

        writer.write(chunk);

        verify(recommendKeywordRepository, never()).saveAll(any());
    }
}