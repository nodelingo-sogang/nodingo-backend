package nodingo.core.user.service;

import nodingo.core.global.exception.keyword.KeywordNotFoundException;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserPersona;
import nodingo.core.user.dto.command.InterestCommand;
import nodingo.core.user.dto.command.SaveOnboardingCommand;
import nodingo.core.user.repository.UserInterestRepository;
import nodingo.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @InjectMocks
    private OnboardingService onboardingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Test
    @DisplayName("온보딩 정보 저장 성공 - 기존 데이터 삭제 후 트리 구조 저장 확인")
    void saveOnboardingInfo_Success() {
        // 1. Given
        Long userId = 1L;

        User realUser = User.create("naver", "sub-123", "yena", "최예나", "yena@yena.com");
        User mockUser = spy(realUser);

        lenient().when(mockUser.getId()).thenReturn(userId);

        Long macroId = 10L;
        List<Long> specificIds = List.of(101L, 102L);

        InterestCommand interestCmd = InterestCommand.builder()
                .macroId(macroId)
                .specificIds(specificIds)
                .build();

        SaveOnboardingCommand command = SaveOnboardingCommand.builder()
                .userId(userId)
                .personas(List.of(UserPersona.TECHNOLOGY))
                .interests(List.of(interestCmd))
                .build();

        Keyword macroK = spy(Keyword.create("기술"));
        Keyword specK1 = spy(Keyword.create("백엔드"));
        Keyword specK2 = spy(Keyword.create("AI"));

        lenient().when(macroK.getId()).thenReturn(macroId);
        lenient().when(specK1.getId()).thenReturn(101L);
        lenient().when(specK2.getId()).thenReturn(102L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(keywordRepository.findAllById(anyList())).thenReturn(List.of(macroK, specK1, specK2));

        // 2. When
        onboardingService.saveOnboardingInfo(command);

        // 3. Then
        verify(userInterestRepository, times(1))
                .deleteTodayInterests(eq(userId), any(LocalDate.class));

        verify(keywordRepository, times(1)).findAllById(anyList());

        verify(mockUser, times(1)).completeOnboarding(any());

        assertThat(mockUser.getInterests()).hasSize(3);

        boolean hasParentRelation = mockUser.getInterests().stream()
                .anyMatch(ui -> ui.getParent() != null && ui.getParent().getKeyword().equals(macroK));

        assertThat(hasParentRelation).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 키워드 ID가 포함된 경우 예외가 발생한다")
    void saveOnboardingInfo_KeywordNotFound() {
        // Given
        Long userId = 1L;
        User realUser = User.create("google", "sub-123", "seongmin", "최성민", "test@test.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(realUser));

        when(keywordRepository.findAllById(anyList())).thenReturn(List.of());

        SaveOnboardingCommand command = SaveOnboardingCommand.builder()
                .userId(userId)
                .personas(List.of(UserPersona.TECHNOLOGY))
                .interests(List.of(InterestCommand.builder()
                        .macroId(999L)
                        .specificIds(List.of())
                        .build()))
                .build();

        // When & Then
        assertThrows(KeywordNotFoundException.class, () -> {
            onboardingService.saveOnboardingInfo(command);
        });
    }
}