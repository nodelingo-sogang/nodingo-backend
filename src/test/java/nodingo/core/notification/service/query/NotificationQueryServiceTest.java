package nodingo.core.notification.service.query;

import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.dto.result.NotificationResult;
import nodingo.core.notification.repository.NotificationSettingRepository;
import nodingo.core.user.domain.User;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private NotificationSetting setting1;

    @Mock
    private NotificationSetting setting2;

    @Test
    @DisplayName("특정 시간의 알림 대상자 목록을 성공적으로 조회한다")
    void getTargetSettings_Success() {
        // given
        int targetHour = 9;
        List<NotificationSetting> expectedSettings = List.of(setting1, setting2);

        given(notificationSettingRepository.findSettingsByHour(targetHour))
                .willReturn(expectedSettings);

        // when
        List<NotificationSetting> actualSettings = notificationQueryService.getTargetSettings(targetHour);

        // then
        assertThat(actualSettings).isNotNull();
        assertThat(actualSettings).hasSize(2);
        assertThat(actualSettings).containsExactly(setting1, setting2);
    }

    @Test
    @DisplayName("대상자가 없을 경우 빈 리스트를 반환한다")
    void getTargetSettings_Empty() {
        // given
        int targetHour = 14;

        given(notificationSettingRepository.findSettingsByHour(targetHour))
                .willReturn(List.of());

        // when
        List<NotificationSetting> actualSettings = notificationQueryService.getTargetSettings(targetHour);

        // then
        assertThat(actualSettings).isEmpty();
    }

    @Test
    @DisplayName("특정 유저의 알림 설정을 성공적으로 조회한다")
    void getNotificationSetting_Success() {
        // given
        Long userId = 1L;
        int hour = 13;
        String token = "fcm_token_test";

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);

        NotificationSetting setting = NotificationSetting.create(user);
        setting.update(hour, token);

        given(notificationSettingRepository.findByUserId(userId))
                .willReturn(Optional.of(setting));

        // when
        NotificationResult result = notificationQueryService.getNotificationSetting(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNotifyHour()).isEqualTo(hour);
        verify(notificationSettingRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("알림 설정이 없는 유저를 조회할 경우 null을 반환한다")
    void getNotificationSetting_ReturnNull_WhenEmpty() {
        // given
        Long userId = 2L;
        given(notificationSettingRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        // when
        NotificationResult result = notificationQueryService.getNotificationSetting(userId);

        // then
        assertThat(result).isNull(); // 성민님이 의도한 null 반환 검증
        verify(notificationSettingRepository, times(1)).findByUserId(userId);
    }
}