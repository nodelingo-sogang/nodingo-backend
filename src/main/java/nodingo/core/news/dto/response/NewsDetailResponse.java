package nodingo.core.news.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.news.dto.result.NewsDetailResult;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDetailResponse {
    private Long id;
    private String title;
    private String body;
    private String url;
    private LocalDateTime dateTimePub;
    private List<String> keywords;

    public static NewsDetailResponse from(NewsDetailResult result) {
        return NewsDetailResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .body(result.getBody())
                .url(result.getUrl())
                .dateTimePub(result.getDateTimePub())
                .keywords(result.getKeywords())
                .build();
    }
}