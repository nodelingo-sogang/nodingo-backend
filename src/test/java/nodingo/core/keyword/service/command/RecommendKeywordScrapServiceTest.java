package nodingo.core.keyword.service.command;

import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendKeywordScrapServiceTest {

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
    private final Long keywordId = 100L;
    private final Long rkId = 500L;

    @Test
    @DisplayName("키워드 스크랩 추가 테스트")
    void addScrap() {
        // given
        RecommendKeyword mockRk = mock(RecommendKeyword.class);
        User mockUser = mock(User.class);

        given(mockRk.getId()).willReturn(rkId);
        given(recommendKeywordRepository.findRecommend(userId, keywordId)).willReturn(Optional.of(mockRk));
        given(userScrapRepository.isKeywordScrapped(userId, rkId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        keywordScrapService.addScrap(userId, keywordId);

        // then
        verify(userScrapRepository, times(1)).save(any(UserScrap.class));
        verify(userVectorService, times(1)).updateKeywordEmbeddingAsync(userId, keywordId);
    }

    @Test
    @DisplayName("키워드 스크랩 삭제 테스트")
    void removeScrap() {
        // given
        RecommendKeyword mockRk = mock(RecommendKeyword.class);
        UserScrap mockScrap = mock(UserScrap.class);

        given(mockRk.getId()).willReturn(rkId);
        given(recommendKeywordRepository.findRecommend(userId, keywordId)).willReturn(Optional.of(mockRk));
        given(userScrapRepository.findKeywordScrap(userId, rkId)).willReturn(Optional.of(mockScrap));

        // when
        keywordScrapService.removeScrap(userId, keywordId);

        // then
        verify(userScrapRepository, times(1)).delete(mockScrap);
    }
}