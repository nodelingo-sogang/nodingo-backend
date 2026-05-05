package nodingo.core.keyword.service.query;

import nodingo.core.ai.dto.keyword.KeywordRecommend;
import nodingo.core.keyword.dto.query.KeywordCandidate;
import nodingo.core.keyword.repository.KeywordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeywordRecommendQueryServiceTest {

    @InjectMocks
    private KeywordRecommendQueryService queryService;

    @Mock
    private KeywordRepository keywordRepository;

    @Test
    @DisplayName("성공: 특정 시간대의 후보 키워드를 조회하여 DTO 리스트로 변환한다")
    void getDailyCandidateKeywords_Success() {
        // 1. Given
        LocalDate targetDate = LocalDate.of(2026, 5, 6);

        LocalDateTime expectedStart = LocalDateTime.of(2026, 5, 5, 5, 1);
        LocalDateTime expectedEnd = LocalDateTime.of(2026, 5, 6, 4, 59);

        KeywordCandidate mockDto = new KeywordCandidate(1L, "아스날", new float[]{0.1f, 0.2f});
        given(keywordRepository.findCandidateKeywords(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(mockDto));

        // 2. When
        List<KeywordRecommend.CandidateKeyword> results = queryService.getDailyCandidateKeywords(targetDate);

        // 3. Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getWord()).isEqualTo("아스날");

        verify(keywordRepository).findCandidateKeywords(
                eq(expectedStart),
                eq(expectedEnd)
        );
    }
}