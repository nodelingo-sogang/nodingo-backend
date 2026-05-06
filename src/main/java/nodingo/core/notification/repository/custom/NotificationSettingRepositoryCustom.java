package nodingo.core.notification.repository.custom;

import nodingo.core.notification.domain.NotificationSetting;
import java.util.List;

public interface NotificationSettingRepositoryCustom {
    List<NotificationSetting> findSettingsByHour(int hour);
}
