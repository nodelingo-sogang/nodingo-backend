package nodingo.core.batch.dto.article;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import nodingo.core.news.domain.News;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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

    public static News toEntity(NewsApiItem item) {
        if (item == null) return null;

        return News.create(
                item.getUri(),
                item.getTitle() != null ? item.getTitle() : "Untitled News",
                item.getBody(),
                item.getUrl(),
                item.getLang(),
                item.getSentiment() != null ? item.getSentiment() : 0.0,
                parseDate(item.getDateTimePub())
        );
    }

    private static LocalDateTime parseDate(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}