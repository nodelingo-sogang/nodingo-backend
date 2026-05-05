package nodingo.core.news.dto.result;

import lombok.Builder;
import lombok.Getter;
import nodingo.core.news.domain.News;
import nodingo.core.user.domain.UserPersona;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NewsDetailResult {
    private Long id;
    private String title;
    private String body;
    private String url;
    private LocalDateTime dateTimePub;
    private List<String> keywords;

    public static NewsDetailResult from(News news) {
        return NewsDetailResult.builder()
                .id(news.getId())
                .title(news.getTitle())
                .body(news.getBody())
                .url(news.getUrl())
                .dateTimePub(news.getDateTimePub())
                .keywords(news.getNewsKeywords().stream()
                        .map(nk -> nk.getKeyword().getWord())
                        .toList())
                .build();
    }
}
