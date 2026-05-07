package nodingo.core.batch.notification.processor;

import com.google.firebase.messaging.Message;
import nodingo.core.notification.domain.NotificationSetting;
import nodingo.core.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import com.google.firebase.messaging.Notification;

class NotificationItemProcessorTest {

    private final NotificationItemProcessor processor = new NotificationItemProcessor();

    @Test
    @DisplayName("알림 설정을 바탕으로 유저 맞춤형 FCM 메시지를 생성한다")
    void processTest() {
        // given
        User user = User.create("google", "12345", "sungmin_choi", "성민", "test@test.com");
        NotificationSetting setting = NotificationSetting.create(user);
        ReflectionTestUtils.setField(setting, "fcmToken", "test-token");

        // when
        Message message = processor.process(setting);

        // then
        assertThat(message).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ReflectionTestUtils.getField(message, "data");
        assertThat(data).isNotNull();
        assertThat(data.get("path")).isEqualTo("/graph/tabs");

        Notification notification = (Notification) ReflectionTestUtils.getField(message, "notification");
        assertThat(notification).isNotNull();

        String title = (String) ReflectionTestUtils.getField(notification, "title");
        String body = (String) ReflectionTestUtils.getField(notification, "body");

        assertThat(title).isEqualTo("🚀 이슈 맵 업데이트 완료!");
        assertThat(body).contains("성민님");
    }
}