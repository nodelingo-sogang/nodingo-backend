package nodingo.core.notification.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /**
     * 유저별 커스텀 시간에 맞춘 알림 발송 및 캐시 초기화
     */
    @Transactional
    @CacheEvict(value = "batch:graph", allEntries = true)
    public void sendDailyNotification(Long userId) {
        log.info("[NotificationService] Sending notification and evicting graph cache for User: {}", userId);

        // TODO: FCM 또는 알림 톡 발송 로직
        // 알림이 나가는 시점 = 어제의 데이터가 구식이 되는 시점
        // 여기서 CacheEvict가 동작하여 다음 접속 시 신규 그래프를 생성하게 함
    }
}