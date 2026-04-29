package nodingo.core.global.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.news.domain.News;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiItem {
    private String uri;
    private String lang;
    private String url;
    private String title;
    private String body;
    private Double sentiment;
    private String dateTimePub;

    public News toEntity() {
        return News.create(
                this.uri,
                this.title,
                this.body,
                this.url,
                this.lang,
                this.sentiment,
                LocalDateTime.parse(this.dateTimePub, DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}
