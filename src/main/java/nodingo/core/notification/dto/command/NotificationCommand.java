package nodingo.core.notification.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nodingo.core.notification.dto.request.NotificationRequest;

@Getter
@AllArgsConstructor
public class NotificationCommand {
    private final Long userId;
    private final int notifyHour;
    private final String fcmToken;

    public static NotificationCommand of(Long userId, NotificationRequest request) {
        return new NotificationCommand(userId, request.getNotifyHour(), request.getFcmToken());
    }
}
