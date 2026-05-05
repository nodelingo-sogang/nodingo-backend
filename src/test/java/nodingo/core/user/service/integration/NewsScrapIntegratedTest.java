package nodingo.core.user.service.integration;

import nodingo.core.global.exception.userScrap.DuplicateScrapException;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserScrap;
import nodingo.core.user.repository.UserRepository;
import nodingo.core.user.repository.UserScrapRepository;
import nodingo.core.user.service.command.NewsScrapService;
import nodingo.core.user.service.vector.UserVectorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsScrapIntegratedTest {

    @InjectMocks
    private NewsScrapService newsScrapService;

    @Mock
    private UserScrapRepository userScrapRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NewsRepository newsRepository;
    @Mock
    private UserVectorService userVectorService;

    private final Long userId = 1L;
    private final Long newsId = 100L;

    @Test
    @DisplayName("성공: 스크랩 추가 시 저장 로직과 비동기 업데이트가 호출되어야 한다")
    void addScrap_Success() {
        // Given
        User mockUser = mock(User.class);
        News mockNews = mock(News.class);

        given(userScrapRepository.existsByUserIdAndNewsId(userId, newsId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(newsRepository.findById(newsId)).willReturn(Optional.of(mockNews));

        // When
        newsScrapService.addScrap(userId, newsId);

        // Then
        verify(userScrapRepository, times(1)).save(any(UserScrap.class));
        verify(userVectorService, times(1)).updateUserEmbeddingAsync(userId, newsId, "SCRAP");
    }

    @Test
    @DisplayName("실패: 이미 스크랩된 뉴스면 DuplicateScrapException이 발생한다")
    void addScrap_Duplicate_Fail() {
        // Given
        given(userScrapRepository.existsByUserIdAndNewsId(userId, newsId)).willReturn(true);

        // When & Then
        assertThrows(DuplicateScrapException.class, () -> newsScrapService.addScrap(userId, newsId));

        verify(userScrapRepository, never()).save(any());
        verify(userVectorService, never()).updateUserEmbeddingAsync(any(), any(), any());
    }

    @Test
    @DisplayName("성공: 스크랩 취소 시 삭제 로직이 수행되어야 한다")
    void removeScrap_Success() {
        // Given
        UserScrap mockScrap = mock(UserScrap.class);
        given(userScrapRepository.findByUserIdAndNewsId(userId, newsId)).willReturn(Optional.of(mockScrap));

        // When
        newsScrapService.removeScrap(userId, newsId);

        // Then
        verify(userScrapRepository, times(1)).delete(mockScrap);
    }
}