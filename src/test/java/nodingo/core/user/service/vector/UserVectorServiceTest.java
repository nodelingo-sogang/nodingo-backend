package nodingo.core.user.service.vector;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.userEmbedding.UserEmbedding;
import nodingo.core.global.exception.ai.AiIntegrationException;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.domain.NewsKeyword;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserScrap;
import nodingo.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserVectorServiceTest {

    @InjectMocks
    private UserVectorService userVectorService;

    @Mock private AiClient aiClient;
    @Mock private UserRepository userRepository;
    @Mock private NewsRepository newsRepository;

    @Test
    @DisplayName("성공: 초기 키워드를 바탕으로 AI 서버에서 임베딩을 받아 유저에게 설정한다")
    void initUserEmbedding_Success() {
        // given
        User user = spy(User.create("google", "sub-123", "sungmin", "성민", "test@test.com"));
        List<Keyword> selectedKeywords = List.of(
                createMockKeyword(1L, "정치", new float[]{0.1f, 0.1f}),
                createMockKeyword(2L, "경제", new float[]{0.2f, 0.2f})
        );

        float[] expectedEmbedding = {0.5f, 0.5f, 0.5f};
        UserEmbedding.Response mockResponse = UserEmbedding.Response.builder()
                .userId(1L)
                .embedding(expectedEmbedding)
                .build();

        given(aiClient.initUserEmbedding(any())).willReturn(mockResponse);

        // when
        userVectorService.initUserEmbedding(user, selectedKeywords);

        // then
        verify(user).updateEmbedding(expectedEmbedding);
        verify(aiClient).initUserEmbedding(any());
    }

    @Test
    @DisplayName("성공: 스크랩 정보를 바탕으로 유저 임베딩을 업데이트하고 저장한다")
    void updateUserEmbeddingAsync_Success() {
        // given
        Long userId = 1L;
        Long newsId = 100L;

        User user = spy(User.create("google", "sub-123", "sungmin", "성민", "test@test.com"));
        News news = mock(News.class);
        UserScrap scrap = mock(UserScrap.class);

        given(newsRepository.findScrapDetail(userId, newsId)).willReturn(Optional.of(scrap));

        org.mockito.Mockito.lenient().when(scrap.getUser()).thenReturn(user);
        org.mockito.Mockito.lenient().when(scrap.getNews()).thenReturn(news);

        // News 내부에 키워드 리스트 세팅
        Keyword mockKeyword = mock(Keyword.class);
        NewsKeyword mockNewsKeyword = mock(NewsKeyword.class);
        org.mockito.Mockito.lenient().when(news.getNewsKeywords()).thenReturn(List.of(mockNewsKeyword));
        org.mockito.Mockito.lenient().when(mockNewsKeyword.getKeyword()).thenReturn(mockKeyword);

        float[] newEmbedding = {0.1f, 0.2f, 0.3f};
        UserEmbedding.Response mockRes = UserEmbedding.Response.builder()
                .userId(userId)
                .embedding(newEmbedding)
                .build();

        given(aiClient.updateUserEmbedding(any())).willReturn(mockRes);

        // when
        userVectorService.updateUserEmbeddingAsync(userId, newsId, "SCRAP");

        // then
        verify(user).updateEmbedding(newEmbedding);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("실패: AI 서버 통신 중 예외 발생 시 AiIntegrationException으로 래핑된다")
    void initUserEmbedding_Fail_CommunicationError() {
        // given
        User user = User.create("google", "sub-123", "sungmin", "성민", "test@test.com");
        given(aiClient.initUserEmbedding(any())).willThrow(new RuntimeException("Connection Refused"));

        // when & then
        assertThrows(AiIntegrationException.class, () ->
                userVectorService.initUserEmbedding(user, List.of(mock(Keyword.class)))
        );
    }

    // --- Helper Method ---
    private Keyword createMockKeyword(Long id, String word, float[] embedding) {
        Keyword k = mock(Keyword.class);
        given(k.getId()).willReturn(id);
        given(k.getWord()).willReturn(word);
        given(k.getEmbedding()).willReturn(embedding);
        return k;
    }
}