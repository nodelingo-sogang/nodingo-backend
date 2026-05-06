package nodingo.core.notification.repository;

import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.repository.custom.NotificationSettingRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>, NotificationSettingRepositoryCustom {
    Optional<NotificationSetting> findByUserId(Long userId);
}