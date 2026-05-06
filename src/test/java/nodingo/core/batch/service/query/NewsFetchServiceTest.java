package nodingo.core.batch.service.query;

import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.article.NewsApiItem;
import nodingo.core.batch.dto.article.NewsApiResponse;
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
    @DisplayName("Article API - 어제 05:01 ~ 오늘 04:59 범위의 뉴스를 본문 포함하여 가져온다")
    void fetchNewsTest() {
        // given
        LocalDate targetDate = LocalDate.now().minusDays(1);
        int page = 1;

        // when
        NewsApiResponse response = newsFetchService.fetchNews(targetDate, page);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getArticles()).isNotNull();

        List<NewsApiItem> results = response.getArticles().getResults();

        assertThat(results)
                .as("기사 목록이 비어 있으면 테스트 진행 불가 (API 데이터 확인 필요)")
                .isNotEmpty();

        NewsApiItem article = results.get(0);

        assertThat(article.getUri()).isNotBlank();
        assertThat(article.getTitle()).isNotBlank();

        assertThat(article.getBody())
                .as("AI 분석을 위해 본문 전체가 수집되어야 함")
                .isNotBlank();

        assertThat(article.getBody().length())
                .as("본문이 너무 짧음 (풀텍스트 수집 여부 확인 필요)")
                .isGreaterThan(100);

        log.info(">>>> [Test Result] Article URI: {}", article.getUri());
        log.info(">>>> [Test Result] Title: {}", article.getTitle());
        log.info(">>>> [Test Result] DateTimePub: {}", article.getDateTimePub());
        log.info(">>>> [Test Result] Body Length: {}", article.getBody().length());
        log.info(">>>> [Test Result] Total Articles in Page: {}", results.size());
    }
}