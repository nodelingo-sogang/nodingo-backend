package nodingo.core.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.NewsApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final RestClient newsApiClient;

    @Value("${news.api.key}")
    private String apiKey;

    public NewsApiResponse fetchNews(LocalDate date, int page, String lang) {
        log.info(">>>> [NewsFetchService] API request - date: {}, page: {}, language: {}",
                date, page, (lang == null ? "KOR/ENG Fixed" : lang));

        try {
            return newsApiClient.post()
                    .uri("/article/getArticles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createRequestBody(date, page, lang))
                    .retrieve()
                    .body(NewsApiResponse.class);
        } catch (Exception e) {
            log.error(">>>> [NewsFetchService] Error occurred during API request: {}", e.getMessage());
            throw new RuntimeException("News API 통신 실패", e);
        }
    }

    private Map<String, Object> createRequestBody(LocalDate date, int page, String lang) {
        Map<String, Object> body = new HashMap<>();
        body.put("action", "getArticles");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);

        body.put("dateStart", twentyFourHoursAgo.toLocalDate().toString());
        body.put("dateEnd", now.toLocalDate().toString());

        body.put("timeStart", twentyFourHoursAgo.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString());
        body.put("timeEnd", now.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString());

        if (lang != null && !lang.isBlank()) {
            body.put("lang", lang);
        } else {
            body.put("lang", new String[]{"kor", "eng"});
        }

        body.put("articlesPage", page);
        body.put("articlesCount", 100);
        body.put("articlesSortBy", "date");
        body.put("resultType", "articles");
        body.put("apiKey", apiKey);

        return body;
    }
}
