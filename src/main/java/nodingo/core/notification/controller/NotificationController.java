package nodingo.core.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.auth.CustomOAuth2User;
import nodingo.core.global.dto.response.ApiResponse;
import nodingo.core.notification.dto.command.NotificationCommand;
import nodingo.core.notification.dto.request.NotificationRequest;
import nodingo.core.notification.service.command.FcmService;
import nodingo.core.notification.service.command.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final FcmService fcmService;

    @Operation(
            summary = "유저 알림 설정 저장",
            description = "유저가 뉴스 요약을 받고 싶은 시간(1~24시)과 FCM 기기 토큰을 저장 및 갱신합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 알림 설정이 저장되었습니다.")
    })
    @PostMapping("/setting")
    public ResponseEntity<ApiResponse<Void>> setNotification(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @RequestBody NotificationRequest request) {
        notificationService.updateNotificationSetting(NotificationCommand.of(customOAuth2User.getUser().getId(), request));
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "성공적으로 알림 설정이 저장되었습니다.", null));
    }

    @Operation(summary = "FCM 단건 전송 임시 테스트", description = "DB에 저장된 토큰을 입력해 직접 알림을 테스트합니다.")
    @PostMapping("/test-push")
    public ResponseEntity<String> testPush(@RequestParam String token) {
        fcmService.sendTestMessage(token);
        return ResponseEntity.ok("FCM 전송 요청 완료! (서버 로그와 기기를 확인하세요)");
    }
}
