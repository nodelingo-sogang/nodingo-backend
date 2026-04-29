package nodingo.core.user.service;

import nodingo.core.global.exception.keyword.KeywordNotFoundException;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.user.domain.InterestLevel;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserInterest;
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

    @Test
    @DisplayName("온보딩 정보 저장 성공 - 단일 페르소나 및 중분류 저장 확인")
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
                .interest(interestCmd)
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
        verify(mockUser, times(1)).completeOnboarding(anyList());
        assertThat(mockUser.getInterests()).hasSize(3);

        UserInterest macroInterest = mockUser.getInterests().stream()
                .filter(ui -> ui.getLevel() == InterestLevel.MACRO)
                .findFirst().orElseThrow();

        assertThat(macroInterest.getKeyword().getId()).isEqualTo(macroId);
    }

    @Test
    @DisplayName("페르소나를 2개 이상 선택할 경우 예외가 발생한다")
    void saveOnboardingInfo_TooManyPersonas() {
        // Given
        Long userId = 1L;
        User realUser = User.create("google", "sub-123", "seongmin", "최성민", "test@test.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(realUser));

        SaveOnboardingCommand command = SaveOnboardingCommand.builder()
                .userId(userId)
                .personas(List.of(UserPersona.TECHNOLOGY, UserPersona.ECONOMY))
                .interest(InterestCommand.builder().macroId(10L).specificIds(List.of()).build())
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            onboardingService.saveOnboardingInfo(command);
        });
    }
}