package nodingo.core.batch.service;

import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.NewsApiItem;
import nodingo.core.batch.dto.NewsApiResponse;
import nodingo.core.batch.service.NewsFetchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class NewsFetchServiceTest {

    @Autowired
    private NewsFetchService newsFetchService;

    @Test
    @DisplayName("Event Registry API 호출 및 데이터 매핑 테스트")
    void fetchNewsTest() {
        // given
        LocalDate targetDate = LocalDate.now().minusDays(1);
        int page = 1;
        String lang = "eng";

        // when
        NewsApiResponse response = newsFetchService.fetchNews(targetDate, page, lang);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getArticles()).isNotNull();

        List<NewsApiItem> results = response.getArticles().getResults();
        assertThat(results).isNotEmpty();

        NewsApiItem firstItem = results.get(0);
        log.info(">>>> [Test Result] Title: {}", firstItem.getTitle());
        log.info(">>>> [Test Result] URI: {}", firstItem.getUri());
        log.info(">>>> [Test Result] Language: {}", firstItem.getLang());
        log.info(">>>> [Test Result] Body Length: {}", firstItem.getBody().length());
        log.info(">>>> [Test Result] Sentiment: {}", firstItem.getSentiment());

        assertThat(firstItem.getBody().length()).isGreaterThan(500);
        assertThat(firstItem.getDateTimePub()).isNotNull();
    }
}