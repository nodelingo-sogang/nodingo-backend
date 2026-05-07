package nodingo.core.notification.service.command;

import nodingo.core.global.exception.user.UserNotFoundException;
import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.dto.command.NotificationCommand;
import nodingo.core.notification.repository.NotificationSettingRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private NotificationCommand command;

    @Mock
    private User user;

    @Mock
    private NotificationSetting notificationSetting;

    @Test
    @DisplayName("알림 설정 업데이트 - 사용자를 찾을 수 없으면 예외가 발생한다")
    void updateNotificationSetting_UserNotFound() {
        // given
        Long userId = 1L;
        given(command.getUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.updateNotificationSetting(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(notificationSettingRepository, never()).findByUserId(any());
        verify(notificationSettingRepository, never()).save(any());
    }

    @Test
    @DisplayName("알림 설정 업데이트 - 기존 설정이 존재하는 경우 바로 업데이트한다")
    void updateNotificationSetting_ExistingSetting() {
        // given
        Long userId = 1L;
        int notifyHour = 9;
        String fcmToken = "test-token";

        given(command.getUserId()).willReturn(userId);
        given(command.getNotifyHour()).willReturn(notifyHour);
        given(command.getFcmToken()).willReturn(fcmToken);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(notificationSettingRepository.findByUserId(userId)).willReturn(Optional.of(notificationSetting));

        // when
        notificationService.updateNotificationSetting(command);

        // then
        verify(notificationSettingRepository, never()).save(any());
        verify(notificationSetting).update(notifyHour, fcmToken);
    }

    @Test
    @DisplayName("알림 설정 업데이트 - 기존 설정이 없으면 새로 생성하여 저장 후 업데이트한다")
    void updateNotificationSetting_NewSetting() {
        // given
        Long userId = 1L;
        int notifyHour = 9;
        String fcmToken = "test-token";

        given(command.getUserId()).willReturn(userId);
        given(command.getNotifyHour()).willReturn(notifyHour);
        given(command.getFcmToken()).willReturn(fcmToken);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(notificationSettingRepository.findByUserId(userId)).willReturn(Optional.empty());

        given(notificationSettingRepository.save(any(NotificationSetting.class))).willReturn(notificationSetting);

        // when
        notificationService.updateNotificationSetting(command);

        // then
        verify(notificationSettingRepository, times(1)).save(any());
        verify(notificationSetting).update(notifyHour, fcmToken);
    }
}