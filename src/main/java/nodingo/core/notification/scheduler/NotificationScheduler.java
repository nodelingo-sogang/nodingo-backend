package nodingo.core.notification.scheduler;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.notification.service.query.NotificationQueryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationQueryService notificationQueryService;

    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        int hour = LocalDateTime.now().getHour();
        if (hour == 0) hour = 24;

        List<NotificationSetting> targets = notificationQueryService.getTargetSettings(hour);

        for (NotificationSetting target : targets) {
            sendPush(target.getFcmToken(), target.getUser().getName());
        }
    }

    private void sendPush(String token, String name) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("오늘의 뉴스 요약이 도착했습니다! 🗞️")
                            .setBody(name + "님, 설정하신 시간에 맞춰 오늘의 핵심 뉴스 맵이 준비되었습니다.")
                            .build())
                    .putData("click_action", "OPEN_GRAPH_TABS")
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("[NotificationService] Push send success -> User: {}", name);

        } catch (FirebaseMessagingException e) {
            log.error("[NotificationService] FCM send error: {}", e.getMessage());
        }
    }
}
