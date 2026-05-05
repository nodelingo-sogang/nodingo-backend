package nodingo.core.keyword.service.command;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.keyword.KeywordRecommend;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserInterest;
import nodingo.core.user.repository.UserInterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeywordRecommendServiceTest {

    @InjectMocks
    private KeywordRecommendService commandService;

    @Mock
    private AiClient aiClient;
    @Mock private KeywordRepository keywordRepository;
    @Mock private RecommendKeywordRepository recommendKeywordRepository;
    @Mock private UserInterestRepository userInterestRepository;

    @Test
    @DisplayName("성공: 유저의 관심사 ID를 체크하여 개인화된 후보군을 AI 서버에 전달하고 저장한다")
    void generateRecommendationForUser_Success() {
        // given
        User user = User.create("naver", "sub-1", "sungmin", "성민", "test@test.com");
        ReflectionTestUtils.setField(user, "id", 1L);
        user.updateEmbedding(new float[]{0.5f, 0.5f});

        LocalDate targetDate = LocalDate.of(2026, 5, 6);

        List<KeywordRecommend.CandidateKeyword> commonCandidates = List.of(
                KeywordRecommend.CandidateKeyword.builder()
                        .keywordId(1L)
                        .word("토트넘")
                        .build(),
                KeywordRecommend.CandidateKeyword.builder()
                        .keywordId(2L)
                        .word("아스날")
                        .build()
        );

        Keyword arsenalKeyword = mock(Keyword.class);
        given(arsenalKeyword.getId()).willReturn(2L);

        UserInterest interest = mock(UserInterest.class);
        given(interest.getKeyword()).willReturn(arsenalKeyword);

        given(userInterestRepository.findByUserId(user.getId()))
                .willReturn(List.of(interest));

        KeywordRecommend.Response mockResponse = KeywordRecommend.Response.builder()
                .recommendKeywords(
                        List.of(
                                KeywordRecommend.RecommendResult.builder()
                                        .userId(user.getId())
                                        .keywordId(2L)
                                        .targetDate(targetDate)
                                        .score(0.99)
                                        .summary("test")
                                        .build()
                        )
                )
                .build();

        given(aiClient.recommendKeywords(any())).willReturn(mockResponse);

        given(keywordRepository.getReferenceById(2L)).willReturn(arsenalKeyword);

        given(recommendKeywordRepository.saveAll(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        commandService.generateRecommendationForUser(user, commonCandidates, targetDate);

        // then
        ArgumentCaptor<KeywordRecommend.Request> requestCaptor =
                ArgumentCaptor.forClass(KeywordRecommend.Request.class);

        verify(aiClient).recommendKeywords(requestCaptor.capture());

        List<KeywordRecommend.CandidateKeyword> sentCandidates =
                requestCaptor.getValue().getCandidateKeywords();

        assertThat(sentCandidates.get(0).isUserInterest()).isFalse();
        assertThat(sentCandidates.get(1).isUserInterest()).isTrue();

        verify(recommendKeywordRepository)
                .deleteByUserIdAndTargetDate(user.getId(), targetDate);

        verify(recommendKeywordRepository).saveAll(any());
    }
}