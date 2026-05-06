package nodingo.core.batch.service.cache;

import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KeywordCacheServiceTest {

    @Mock
    private KeywordRepository keywordRepository;

    @InjectMocks
    private KeywordCacheService keywordCacheService;

    @Test
    @DisplayName("DB에서 조회한 키워드 목록을 캐싱용 DTO로 정상 변환한다")
    void getAllKeywords() {
        // given
        Keyword keyword = Keyword.create("테스트");
        ReflectionTestUtils.setField(keyword, "id", 1L);
        keyword.updateEmbedding(new float[]{0.1f, 0.2f});

        given(keywordRepository.findAll()).willReturn(List.of(keyword));

        // when
        List<NewsBatch.ExistingKeywordInput> result = keywordCacheService.getAllKeywords();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeywordId()).isEqualTo(1L);
        assertThat(result.get(0).getWord()).isEqualTo("테스트");
        assertThat(result.get(0).getEmbedding()).containsExactly(0.1f, 0.2f);
    }
}