package nodingo.core.news.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.auth.CustomOAuth2User;
import nodingo.core.global.dto.response.ApiResponse;
import nodingo.core.news.dto.response.NewsDetailResponse;
import nodingo.core.news.dto.result.NewsDetailResult;
import nodingo.core.news.service.NewsQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "News", description = "뉴스 관련 API")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsQueryService newsQueryService;

    @Operation(
            summary = "뉴스 상세 조회",
            description = "뉴스 고유 ID를 통해 본문 및 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 뉴스 상세 내용을 조회했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 뉴스 아이디입니다.")
    })
    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable Long newsId) {
        NewsDetailResult result = newsQueryService.getNewsDetail(newsId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "성공적으로 뉴스 상세 내용을 조회했습니다.", NewsDetailResponse.from(result)));
    }
}
