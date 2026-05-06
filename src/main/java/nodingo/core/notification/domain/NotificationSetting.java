package nodingo.core.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.user.domain.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "notify_hour")
    private Integer notifyHour;

    public static NotificationSetting create(User user) {
        NotificationSetting setting = new NotificationSetting();
        setting.user = user;
        return setting;
    }

    public void update(int hour, String token) {
        this.notifyHour = hour;
        this.fcmToken = token;
    }
}
