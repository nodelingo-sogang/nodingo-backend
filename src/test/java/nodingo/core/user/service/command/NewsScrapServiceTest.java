package nodingo.core.user.service.command;

import nodingo.core.global.exception.userScrap.DuplicateScrapException;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserScrap;
import nodingo.core.user.repository.UserRepository;
import nodingo.core.user.repository.UserScrapRepository;
import nodingo.core.user.service.vector.UserVectorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsScrapServiceTest {

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
    @DisplayName("성공: 스크랩 추가 시 데이터가 저장되고 비동기 업데이트가 호출된다")
    void addScrap_Success() {
        // Given
        User user = mock(User.class);
        News news = mock(News.class);

        // ifScrapped 통과
        given(userScrapRepository.existsByUserIdAndNewsId(userId, newsId)).willReturn(false);
        // createUserScrap 내부 로직
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(newsRepository.findById(newsId)).willReturn(Optional.of(news));

        // When
        newsScrapService.addScrap(userId, newsId);

        // Then
        verify(userScrapRepository, times(1)).save(any(UserScrap.class));
        verify(userVectorService, times(1)).updateUserEmbeddingAsync(userId, newsId, "SCRAP");
    }

    @Test
    @DisplayName("실패: 이미 스크랩된 경우 DuplicateScrapException이 발생한다")
    void addScrap_Fail_AlreadyScrapped() {
        // Given
        given(userScrapRepository.existsByUserIdAndNewsId(userId, newsId)).willReturn(true);

        // When & Then
        DuplicateScrapException exception = assertThrows(DuplicateScrapException.class,
                () -> newsScrapService.addScrap(userId, newsId));

        assertThat(exception.getMessage()).isEqualTo("이미 스크랩한 뉴스입니다.");
        verify(userScrapRepository, never()).save(any());
        verify(userVectorService, never()).updateUserEmbeddingAsync(any(), any(), any());
    }

    @Test
    @DisplayName("성공: 스크랩 취소 시 해당 스크랩 데이터를 삭제한다")
    void removeScrap_Success() {
        // Given
        UserScrap scrap = mock(UserScrap.class);
        given(userScrapRepository.findByUserIdAndNewsId(userId, newsId)).willReturn(Optional.of(scrap));

        // When
        newsScrapService.removeScrap(userId, newsId);

        // Then
        verify(userScrapRepository, times(1)).delete(scrap);
    }

    @Test
    @DisplayName("실패: 삭제하려는 스크랩이 없으면 DuplicateScrapException이 발생한다")
    void removeScrap_Fail_NotFound() {
        // Given
        given(userScrapRepository.findByUserIdAndNewsId(userId, newsId)).willReturn(Optional.empty());

        // When & Then
        DuplicateScrapException exception = assertThrows(DuplicateScrapException.class,
                () -> newsScrapService.removeScrap(userId, newsId));

        assertThat(exception.getMessage()).isEqualTo("스크랩하지 않은 뉴스입니다.");
        verify(userScrapRepository, never()).delete(any());
    }
}