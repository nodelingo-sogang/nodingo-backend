package nodingo.core.news.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.auth.CustomOAuth2User;
import nodingo.core.global.dto.response.ApiResponse;
import nodingo.core.news.service.command.NewsScrapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "News Scrap", description = "뉴스 스크랩 추가 및 취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news/{newsId}/scrap")
public class NewsScrapController {

    private final NewsScrapService newsScrapService;

    @Operation(summary = "뉴스 스크랩 추가", description = "특정 뉴스를 스크랩합니다. 이미 스크랩한 경우 409 에러가 발생합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "뉴스 스크랩 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "뉴스/사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 스크랩한 뉴스")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addScrap(
            @AuthenticationPrincipal CustomOAuth2User customUser,
            @PathVariable Long newsId) {
        newsScrapService.addScrap(customUser.getUser().getId(), newsId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, 201, "뉴스 스크랩 성공", null));
    }

    @Operation(summary = "뉴스 스크랩 취소", description = "스크랩을 취소합니다. 스크랩하지 않은 상태면 409 에러가 발생합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "뉴스 스크랩 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "스크랩하지 않은 뉴스")
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeScrap(
            @AuthenticationPrincipal CustomOAuth2User customUser,
            @PathVariable Long newsId) {
        newsScrapService.removeScrap(customUser.getUser().getId(), newsId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "뉴스 스크랩 취소 성공", null));
    }
}
