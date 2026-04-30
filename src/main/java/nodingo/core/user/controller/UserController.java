package nodingo.core.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.auth.CustomOAuth2User;
import nodingo.core.global.dto.response.ApiResponse;
import nodingo.core.user.domain.User;
import nodingo.core.user.dto.command.SaveOnboardingCommand;
import nodingo.core.user.dto.request.OnboardingRequest;
import nodingo.core.user.service.OnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final OnboardingService onboardingService;

    @Operation(
            summary = "유저 온보딩 관심사 설정",
            description = "인증된 유저 정보를 바탕으로 페르소나 및 관심 키워드를 저장합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공적으로 온보딩 관심사 설정을 완료했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @RequestBody OnboardingRequest request) {
        User loginUser = customOAuth2User.getUser();
        onboardingService.saveOnboardingInfo(SaveOnboardingCommand.from(loginUser.getId(), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, 201, "성공적으로 온보딩 관심사 설정을 완료했습니다."));
    }
}