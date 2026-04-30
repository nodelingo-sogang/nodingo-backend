package nodingo.core.batch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nodingo.core.batch.scheduler.NewsScheduler;
import nodingo.core.global.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Batch", description = "관리자용 배치 실행 API")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final NewsScheduler newsScheduler;

    @Operation(
            summary = "뉴스 수집 배치 수동 실행",
            description = "새벽 5시 스케줄러와 별개로, 즉시 뉴스 데이터를 수집하고 요약하는 배치를 실행합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치 실행 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배치 실행 중 서버 에러 발생")
    })
    @SecurityRequirements(value = {})
    @PostMapping("/news-collect")
    public ResponseEntity<ApiResponse<Void>> triggerNewsJob() {
        try {
            newsScheduler.runDailyNewsJob();
            return ResponseEntity.ok(new ApiResponse<>(true,200, "뉴스 수집 배치가 성공적으로 트리거되었습니다.")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "배치 실행 중 오류가 발생했습니다"));
        }
    }
}
