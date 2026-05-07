package nodingo.core.keyword.integration;

import nodingo.core.global.exception.recommendKeyword.RecommendKeywordNotFoundException;
import nodingo.core.global.exception.scrap.DuplicateScrapException;
import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import nodingo.core.keyword.service.command.RecommendKeywordScrapService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendKeywordScrapIntegratedTest {

    @InjectMocks
    private RecommendKeywordScrapService keywordScrapService;

    @Mock
    private UserScrapRepository userScrapRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecommendKeywordRepository recommendKeywordRepository;
    @Mock
    private UserVectorService userVectorService;

    private final Long userId = 1L;
    private final Long keywordId = 500L;
    private final Long rkId = 999L;

    @Test
    @DisplayName("성공: 키워드 요약 스크랩 추가 시 저장 로직과 AI 비동기 업데이트가 호출된다")
    void addScrap_Success() {
        // Given
        User mockUser = mock(User.class);
        RecommendKeyword mockRk = mock(RecommendKeyword.class);

        given(mockRk.getId()).willReturn(rkId);
        given(recommendKeywordRepository.findRecommend(userId, keywordId)).willReturn(Optional.of(mockRk));
        given(userScrapRepository.isKeywordScrapped(userId, rkId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // When
        keywordScrapService.addScrap(userId, keywordId);

        // Then
        verify(userScrapRepository, times(1)).save(any(UserScrap.class));
        verify(userVectorService, times(1)).updateKeywordEmbeddingAsync(userId, keywordId);
    }

    @Test
    @DisplayName("실패: 이미 스크랩된 키워드 요약이면 DuplicateScrapException이 발생한다")
    void addScrap_Duplicate_Fail() {
        // Given
        RecommendKeyword mockRk = mock(RecommendKeyword.class);
        given(mockRk.getId()).willReturn(rkId);
        given(recommendKeywordRepository.findRecommend(userId, keywordId)).willReturn(Optional.of(mockRk));

        // QueryDSL 기반 exists 체크 메서드
        given(userScrapRepository.isKeywordScrapped(userId, rkId)).willReturn(true);

        // When & Then
        assertThrows(DuplicateScrapException.class, () -> keywordScrapService.addScrap(userId, keywordId));

        verify(userScrapRepository, never()).save(any());
        verify(userVectorService, never()).updateKeywordEmbeddingAsync(anyLong(), anyLong());
    }

    @Test
    @DisplayName("성공: 스크랩 취소 시 삭제 로직이 수행되어야 한다")
    void removeScrap_Success() {
        // Given
        RecommendKeyword mockRk = mock(RecommendKeyword.class);
        UserScrap mockScrap = mock(UserScrap.class);

        given(mockRk.getId()).willReturn(rkId);
        given(recommendKeywordRepository.findRecommend(userId, keywordId)).willReturn(Optional.of(mockRk));

        // QueryDSL 기반 Optional 조회 메서드
        given(userScrapRepository.findKeywordScrap(userId, rkId)).willReturn(Optional.of(mockScrap));

        // When
        keywordScrapService.removeScrap(userId, keywordId);

        // Then
        verify(userScrapRepository, times(1)).delete(mockScrap);
    }

    @Test
    @DisplayName("실패: 추천 정보(RK)가 없으면 RecommendKeywordNotFoundException이 발생한다")
    void addScrap_NoRk_Fail() {
        // Given
        given(recommendKeywordRepository.findRecommend(userId, keywordId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(RecommendKeywordNotFoundException.class, () -> keywordScrapService.addScrap(userId, keywordId));
    }
}