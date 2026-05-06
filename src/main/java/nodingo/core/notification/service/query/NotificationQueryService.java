package nodingo.core.notification.service.query;

import lombok.RequiredArgsConstructor;
import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.repository.NotificationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {
    private final NotificationSettingRepository notificationSettingRepository;

    public List<NotificationSetting> getTargetSettings(int hour) {
        return notificationSettingRepository.findSettingsByHour(hour);
    }
}