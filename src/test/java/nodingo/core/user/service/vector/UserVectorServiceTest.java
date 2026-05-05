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

    @Mock
    private AiClient aiClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NewsRepository newsRepository;

    @Test
    @DisplayName("성공: 초기 키워드를 바탕으로 AI 서버에서 임베딩을 받아 유저에게 설정한다")
    void initUserEmbedding_Success() {
        // 1. Given
        User user = spy(User.create("google", "sub-123", "sungmin", "성민", "test@test.com"));

        Keyword k1 = mock(Keyword.class);
        given(k1.getId()).willReturn(1L);
        given(k1.getWord()).willReturn("정치");
        given(k1.getEmbedding()).willReturn(new float[]{0.1f, 0.1f});

        Keyword k2 = mock(Keyword.class);
        given(k2.getId()).willReturn(2L);
        given(k2.getWord()).willReturn("경제");
        given(k2.getEmbedding()).willReturn(new float[]{0.2f, 0.2f});

        List<Keyword> selectedKeywords = List.of(k1, k2);

        float[] expectedEmbedding = {0.5f, 0.5f, 0.5f};
        UserEmbedding.Response mockResponse = new UserEmbedding.Response(1L, expectedEmbedding);

        given(aiClient.initUserEmbedding(any(UserEmbedding.InitRequest.class))).willReturn(mockResponse);

        // 2. When
        userVectorService.initUserEmbedding(user, selectedKeywords);

        // 3. Then
        verify(user, times(1)).updateEmbedding(expectedEmbedding);
        verify(aiClient, times(1)).initUserEmbedding(any());
    }

    @Test
    @DisplayName("실패: AI 서버 응답이 비어있으면 AiIntegrationException이 발생한다")
    void initUserEmbedding_Fail_EmptyResponse() {
        // Given
        User user = User.create("google", "sub-123", "sungmin", "성민", "test@test.com");
        List<Keyword> keywords = List.of(mock(Keyword.class));

        given(aiClient.initUserEmbedding(any())).willReturn(null);

        // When & Then
        assertThrows(AiIntegrationException.class, () ->
                userVectorService.initUserEmbedding(user, keywords)
        );
    }

    @Test
    @DisplayName("실패: AI 서버 통신 중 예외가 발생하면 AiIntegrationException으로 래핑되어 던져진다")
    void initUserEmbedding_Fail_CommunicationError() {
        // Given
        User user = User.create("google", "sub-123", "sungmin", "성민", "test@test.com");
        List<Keyword> keywords = List.of(mock(Keyword.class));

        given(aiClient.initUserEmbedding(any())).willThrow(new RuntimeException("Connection Refused"));

        // When & Then
        assertThrows(AiIntegrationException.class, () ->
                userVectorService.initUserEmbedding(user, keywords)
        );
    }

    @Test
    @DisplayName("성공: AI 서버 응답을 받아 유저 임베딩을 업데이트하고 저장한다")
    void updateUserEmbeddingAsync_Success() {
        // Given
        Long userId = 1L, newsId = 100L;

        User realUser = User.create("google", "sub-123", "sungmin_test", "성민", "test@test.com");
        User user = spy(realUser);

        UserScrap scrap = mock(UserScrap.class);
        News news = mock(News.class);

        float[] newEmbedding = {0.1f, 0.2f, 0.3f};
        UserEmbedding.Response mockRes = new UserEmbedding.Response(userId, newEmbedding);

        given(newsRepository.findScrapDetail(userId, newsId)).willReturn(Optional.of(scrap));
        given(scrap.getUser()).willReturn(user);
        given(scrap.getNews()).willReturn(news);

        NewsKeyword mockNewsKeyword = mock(NewsKeyword.class);
        Keyword mockKeyword = mock(Keyword.class);
        given(news.getNewsKeywords()).willReturn(List.of(mockNewsKeyword));
        given(mockNewsKeyword.getKeyword()).willReturn(mockKeyword);

        given(aiClient.updateUserEmbedding(any())).willReturn(mockRes);

        // When
        userVectorService.updateUserEmbeddingAsync(userId, newsId, "SCRAP");

        // Then
        verify(user, times(1)).updateEmbedding(newEmbedding);
        verify(userRepository, times(1)).saveAndFlush(user);
    }
}