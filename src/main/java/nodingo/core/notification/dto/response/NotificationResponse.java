package nodingo.core.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import nodingo.core.notification.dto.result.NotificationResult;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Integer notifyHour;
    private boolean isSet;

    public static NotificationResponse from(NotificationResult result) {
        if (result == null || result.getNotifyHour() == null) {
            return NotificationResponse.builder()
                    .isSet(false)
                    .build();
        }

        return NotificationResponse.builder()
                .notifyHour(result.getNotifyHour())
                .isSet(true)
                .build();
    }
}
