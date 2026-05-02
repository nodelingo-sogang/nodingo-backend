package nodingo.core.batch.service;

import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.event.Concept;
import nodingo.core.batch.dto.event.EventApiItem;
import nodingo.core.batch.dto.event.EventApiResponse;
import nodingo.core.batch.dto.event.NewsApiItem;
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
    @DisplayName("Event Registry - getEvents로 keyword 확보 후 article API로 본문 조회 테스트")
    void fetchEventsTest() {
        // given
        LocalDate targetDate = LocalDate.now();
        int page = 1;

        // when
        EventApiResponse response = newsFetchService.fetchEvents(targetDate, page);

        // then: event 응답 검증
        assertThat(response).isNotNull();
        assertThat(response.getEvents()).isNotNull();

        List<EventApiItem> results = response.getEvents().getResults();

        assertThat(results)
                .as("events.results가 비어 있음")
                .isNotNull()
                .isNotEmpty();

        EventApiItem event = results.stream()
                .filter(e -> getRepresentativeArticle(e) != null)
                .filter(e -> getRepresentativeArticle(e).getUri() != null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("infoArticle.uri가 있는 event를 찾지 못함"));

        // keyword 검증
        assertThat(event.getConcepts())
                .as("event.concepts는 keyword 저장용이므로 필수")
                .isNotNull()
                .isNotEmpty();

        Concept concept = event.getConcepts().get(0);
        String keyword = getConceptLabel(concept);

        assertThat(keyword)
                .as("concept.label.kor 또는 concept.label.eng가 있어야 keyword 저장 가능")
                .isNotBlank();

        // article uri 확보
        NewsApiItem infoArticle = getRepresentativeArticle(event);

        assertThat(infoArticle).isNotNull();
        assertThat(infoArticle.getUri())
                .as("article 본문 재조회용 uri는 필수")
                .isNotBlank();

        // when: article 상세 조회
        NewsApiItem fullArticle = newsFetchService.fetchArticle(infoArticle.getUri());

        // then: 본문 검증
        assertThat(fullArticle).isNotNull();

        assertThat(fullArticle.getBody())
                .as("뉴스 본문 저장 필수")
                .isNotBlank();

        assertThat(fullArticle.getBody().length())
                .as("본문 길이가 너무 짧으면 full body가 아닐 가능성 있음")
                .isGreaterThan(100);

        log.info(">>>> [Test Result] Event URI: {}", event.getUri());
        log.info(">>>> [Test Result] Event Title: {}", getTitle(event));
        log.info(">>>> [Test Result] Keyword: {}", keyword);
        log.info(">>>> [Test Result] InfoArticle URI: {}", infoArticle.getUri());
        log.info(">>>> [Test Result] Full Article URI: {}", fullArticle.getUri());
        log.info(">>>> [Test Result] Full Article URL: {}", fullArticle.getUrl());
        log.info(">>>> [Test Result] Full Article Lang: {}", fullArticle.getLang());
        log.info(">>>> [Test Result] Full Article Body Length: {}", fullArticle.getBody().length());
    }

    private NewsApiItem getRepresentativeArticle(EventApiItem event) {
        if (event == null || event.getInfoArticle() == null) {
            return null;
        }

        if (event.getInfoArticle().getKor() != null) {
            return event.getInfoArticle().getKor();
        }

        if (event.getInfoArticle().getEng() != null) {
            return event.getInfoArticle().getEng();
        }

        return null;
    }

    private String getTitle(EventApiItem event) {
        if (event == null || event.getTitle() == null) {
            return null;
        }

        if (event.getTitle().getKor() != null && !event.getTitle().getKor().isBlank()) {
            return event.getTitle().getKor();
        }

        if (event.getTitle().getEng() != null && !event.getTitle().getEng().isBlank()) {
            return event.getTitle().getEng();
        }

        return null;
    }

    private String getConceptLabel(Concept concept) {
        if (concept == null || concept.getLabel() == null) {
            return null;
        }

        if (concept.getLabel().getKor() != null && !concept.getLabel().getKor().isBlank()) {
            return concept.getLabel().getKor();
        }

        if (concept.getLabel().getEng() != null && !concept.getLabel().getEng().isBlank()) {
            return concept.getLabel().getEng();
        }

        return null;
    }
}