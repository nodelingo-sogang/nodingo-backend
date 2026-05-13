package nodingo.core.notification.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nodingo.core.notification.domain.NotificationSetting;

@Getter
@AllArgsConstructor
public class NotificationResult {
    private final Long userId;
    private final Integer notifyHour;
    private final String fcmToken;

    public static NotificationResult from(NotificationSetting setting) {
        if (setting == null) return null;

        return new NotificationResult(
                setting.getUser().getId(),
                setting.getNotifyHour(),
                setting.getFcmToken()
        );
    }
}
