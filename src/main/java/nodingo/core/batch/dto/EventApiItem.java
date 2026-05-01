package nodingo.core.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.news.domain.News;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventApiItem {
    private String uri;
    private EventTitle title;
    private String eventDate;

    @JsonProperty("sentiment") 
    private Double sentiment;

    private List<Concept> concepts;
    private InfoArticleWrapper infoArticle;

    public News toNewsEntity() {
        // 1. 한국어 기사 데이터가 있는지 우선 확인
        NewsApiItem article = (this.infoArticle.getKor() != null) ?
                this.infoArticle.getKor() : this.infoArticle.getEng();

        if (article == null) {
            log.warn(">>>> [DTO] No article found for event: {}", this.uri);
            return null;
        }

        String finalTitle = (this.title.getKor() != null) ?
                this.title.getKor() : this.title.getEng();

        LocalDateTime publishedAt;
        try {
            publishedAt = OffsetDateTime.parse(article.getDateTimePub()).toLocalDateTime();
        } catch (Exception e) {
            publishedAt = LocalDateTime.now();
        }

        return News.create(
                article.getUri(),
                finalTitle,
                article.getBody(),
                article.getUrl(),
                article.getLang(),
                (this.sentiment != null ? this.sentiment : 0.0),
                publishedAt
        );
    }
}