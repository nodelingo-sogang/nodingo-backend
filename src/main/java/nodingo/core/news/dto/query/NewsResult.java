package nodingo.core.news.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NewsResult {
    private Long id;
    private String newsUri;
    private String title;
    private String body;
    private String url;
    private String imageUrl;
    private String language;
    private LocalDateTime publishedAt;

    @QueryProjection
    public NewsResult(Long id, String newsUri, String title, String body,
                      String url, String imageUrl, String language, LocalDateTime publishedAt) {
        this.id = id;
        this.newsUri = newsUri;
        this.title = title;
        this.body = body;
        this.url = url;
        this.imageUrl = imageUrl;
        this.language = language;
        this.publishedAt = publishedAt;
    }
}
