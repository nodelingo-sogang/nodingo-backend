package nodingo.core.news.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NewsResult {
    private Long id;
    private String uri;
    private String title;
    private String body;
    private String url;
    private String lang;
    private LocalDateTime dateTimePub;

    @QueryProjection
    public NewsResult(Long id, String uri, String title, String body,
                      String url, String lang, LocalDateTime dateTimePub) {
        this.id = id;
        this.uri = uri;
        this.title = title;
        this.body = body;
        this.url = url;
        this.lang = lang;
        this.dateTimePub = dateTimePub;
    }
}
