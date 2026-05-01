package nodingo.core.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.article.ArticleApiResponse;
import nodingo.core.batch.dto.EventApiResponse;
import nodingo.core.batch.dto.NewsApiItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final RestClient newsApiClient;

    @Value("${news.api.key}")
    private String apiKey;

    public EventApiResponse fetchEvents(LocalDate date, int page) {
        log.info(">>>> [NewsFetchService] Event API request - date: {}, page: {}", date, page);

        try {
            EventApiResponse response = newsApiClient.post()
                    .uri("/event/getEvents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createEventRequestBody(date, page))
                    .retrieve()
                    .body(EventApiResponse.class);

            if (response == null) {
                throw new IllegalStateException("Event Registry API response body is null");
            }

            return response;

        } catch (Exception e) {
            log.error(">>>> [NewsFetchService] Error occurred during Event API request", e);
            throw new RuntimeException("Event Registry API 통신 및 매핑 실패", e);
        }
    }

    public NewsApiItem fetchArticle(String articleUri) {
        log.info(">>>> [NewsFetchService] Article API request - articleUri: {}", articleUri);

        try {
            ArticleApiResponse response = newsApiClient.post()
                    .uri("/article/getArticles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createArticleRequestBody(articleUri))
                    .retrieve()
                    .body(ArticleApiResponse.class);

            if (response == null || response.getFirstArticle() == null) {
                throw new IllegalStateException("Article API response body is null. articleUri=" + articleUri);
            }

            return response.getFirstArticle();

        } catch (Exception e) {
            log.error(">>>> [NewsFetchService] Error occurred during Article API request", e);
            throw new RuntimeException("Event Registry Article API 통신 및 매핑 실패", e);
        }
    }

    private Map<String, Object> createEventRequestBody(LocalDate date, int page) {
        Map<String, Object> body = new HashMap<>();

        LocalDateTime endDateTime = date.atTime(5, 0, 0);
        LocalDateTime startDateTime = endDateTime.minusDays(1);

        body.put("apiKey", apiKey);

        body.put("resultType", "events");
        body.put("eventsPage", page);
        body.put("eventsCount", 50);
        body.put("eventsSortBy", "date");

        body.put("dateStart", startDateTime.toLocalDate().toString());
        body.put("dateEnd", endDateTime.toLocalDate().toString());

        body.put("lang", "kor");
        body.put("conceptLang", "kor");

        // keyword 저장용
        body.put("includeEventConcepts", true);

        // 대표 기사 uri 확보용
        body.put("includeEventInfoArticle", true);

        // 문서상 존재하는 event infoArticle body len 옵션
        body.put("eventsArticleBodyLen", -1);

        log.info(">>>> [Batch Request Range] {} 05:00:00 ~ {} 05:00:00",
                startDateTime.toLocalDate(), endDateTime.toLocalDate());

        return body;
    }

    private Map<String, Object> createArticleRequestBody(String articleUri) {
        Map<String, Object> body = new HashMap<>();

        body.put("apiKey", apiKey);

        // 중요: /article/getArticles 응답
        body.put("resultType", "articles");
        body.put("articlesPage", 1);
        body.put("articlesCount", 1);

        // event.infoArticle.uri로 기사 재조회
        body.put("articleUri", articleUri);

        // 본문 저장용
        body.put("includeArticleBasicInfo", true);
        body.put("includeArticleTitle", true);
        body.put("includeArticleBody", true);
        body.put("includeArticleUrl", true);
        body.put("includeArticleImage", true);
        body.put("includeArticleSentiment", true);
        body.put("includeArticleConcepts", true);

        body.put("articleBodyLen", -1);

        return body;
    }
}