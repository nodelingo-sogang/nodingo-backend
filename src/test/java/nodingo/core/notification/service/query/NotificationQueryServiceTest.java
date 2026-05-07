package nodingo.core.notification.service.query;

import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.repository.NotificationSettingRepository;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
}