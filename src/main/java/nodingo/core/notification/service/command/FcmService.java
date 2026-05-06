package nodingo.core.notification.service.command;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendTestMessage(String targetToken) {
        try {
            Notification notification = Notification.builder()
                    .setTitle("노딩고 테스트 알림 🚀")
                    .setBody("성민님, 서버에서 보낸 알림이 완벽하게 도착했습니다!")
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM push successful! Firebase response: {}", response);

        } catch (Exception e) {
            log.error("FCM push failed. Check token or firebase config: ", e);
        }
    }
}
