package nodingo.core.keyword.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.auth.CustomOAuth2User;
import nodingo.core.global.dto.response.ApiResponse;
import nodingo.core.keyword.service.command.RecommendKeywordScrapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Keyword Scrap", description = "그래프 노드(키워드 요약) 스크랩 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords/{keywordId}/scrap")
public class RecommendKeywordScrapController {

    private final RecommendKeywordScrapService keywordScrapService;

    @Operation(
            summary = "키워드 요약 스크랩 추가",
            description = "그래프 노드의 상세 요약 정보를 내 보관함에 저장합니다. 이미 스크랩한 경우 409 에러가 발생합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "키워드 스크랩 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "키워드/사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 스크랩한 키워드")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addScrap(
            @AuthenticationPrincipal CustomOAuth2User customUser,
            @PathVariable Long keywordId) {
        keywordScrapService.addScrap(customUser.getUser().getId(), keywordId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, 201, "키워드 스크랩 성공", null));
    }

    @Operation(
            summary = "키워드 요약 스크랩 취소",
            description = "스크랩한 키워드 요약을 보관함에서 삭제합니다. 스크랩하지 않은 상태면 409 에러가 발생합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "키워드 스크랩 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "스크랩하지 않은 키워드")
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeScrap(
            @AuthenticationPrincipal CustomOAuth2User customUser,
            @PathVariable Long keywordId) {
        keywordScrapService.removeScrap(customUser.getUser().getId(), keywordId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "키워드 스크랩 취소 성공", null));
    }
}
