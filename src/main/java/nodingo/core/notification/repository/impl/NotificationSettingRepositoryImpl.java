package nodingo.core.notification.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.repository.custom.NotificationSettingRepositoryCustom;

import java.util.List;

import static nodingo.core.notification.domain.QNotificationSetting.notificationSetting;
import static nodingo.core.user.domain.QUser.user;

@RequiredArgsConstructor
public class NotificationSettingRepositoryImpl implements NotificationSettingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NotificationSetting> findSettingsByHour(int hour) {
        return queryFactory
                .selectFrom(notificationSetting)
                .join(notificationSetting.user, user).fetchJoin()
                .where(notificationSetting.notifyHour.eq(hour))
                .fetch();
    }
}
