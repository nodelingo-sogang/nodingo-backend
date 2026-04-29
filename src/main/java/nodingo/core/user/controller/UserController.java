package nodingo.core.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.dto.response.ApiResponse;
import nodingo.core.user.dto.command.SaveOnboardingCommand;
import nodingo.core.user.dto.request.OnboardingRequest;
import nodingo.core.user.service.OnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final OnboardingService onboardingService;

    @Operation(
            summary = "유저 온보딩 관심사 저장",
            description = "유저 ID와 온보딩 선택 데이터를 받아 페르소나 및 3단계 관심 키워드(중/소분류)를 저장합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 온보딩 관심사 설정을 완료했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터 (페르소나 개수 초과 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 또는 키워드를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/onboarding/{userId}")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody OnboardingRequest request) {
        onboardingService.saveOnboardingInfo(SaveOnboardingCommand.from(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, 201, "성공적으로 온보딩 관심사 설정을 완료했습니다."));
    }
}