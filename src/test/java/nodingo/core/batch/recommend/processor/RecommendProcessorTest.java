package nodingo.core.batch.recommend.processor;

import nodingo.core.ai.dto.keyword.KeywordRecommend;
import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.service.command.KeywordRecommendService;
import nodingo.core.keyword.service.query.KeywordRecommendQueryService;
import nodingo.core.user.domain.User;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RecommendProcessorTest {

    @Mock
    private KeywordRecommendQueryService queryService;

    @Mock
    private KeywordRecommendService commandService;

    @InjectMocks
    private RecommendProcessor recommendProcessorConfig;

    @Test
    @DisplayName("Processor가 올바르게 후보군을 조회하고 추천 결과를 반환해야 한다")
    void recommendProcessorTest() throws Exception {
        // given
        User mockUser = User.create("naver", "1234567890", "testUser", "홍길동", "test@test.com");

        List<KeywordRecommend.CandidateKeyword> mockCandidates = List.of(mock(KeywordRecommend.CandidateKeyword.class));
        List<RecommendKeyword> mockRecommendations = List.of(mock(RecommendKeyword.class));

        when(queryService.getDailyCandidateKeywords(any(LocalDate.class)))
                .thenReturn(mockCandidates);

        when(commandService.generateRecommendationForUser(eq(mockUser), eq(mockCandidates), any(LocalDate.class)))
                .thenReturn(mockRecommendations);

        ItemProcessor<User, List<RecommendKeyword>> processor = recommendProcessorConfig.recommendItemProcessor();

        // when
        List<RecommendKeyword> result = processor.process(mockUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockRecommendations);

        verify(queryService, times(1)).getDailyCandidateKeywords(any(LocalDate.class));
        verify(commandService, times(1)).generateRecommendationForUser(eq(mockUser), eq(mockCandidates), any(LocalDate.class));
    }
}