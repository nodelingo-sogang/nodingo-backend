package nodingo.core.global.config.news;

import nodingo.core.batch.dto.event.*;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.Chunk;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class NewsBatchConfigTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsFetchService newsFetchService;

    @Mock
    private KeywordRepository keywordRepository;

    private NewsBatchConfig config;

    @BeforeEach
    void setUp() {
        config = new NewsBatchConfig(
                newsFetchService,
                null,
                null,
                newsRepository,
                keywordRepository,
                null
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Reader는 EventApiResponse를 받아 EventApiItem Iterator를 정상적으로 반환한다")
    void newsReader() throws Exception {
        // given
        EventApiItem eventItem = new EventApiItem();
        setField(eventItem, "uri", "event-1");

        EventWrapper wrapper = new EventWrapper();
        setField(wrapper, "results", List.of(eventItem));
        setField(wrapper, "pages", 1);

        EventApiResponse response = new EventApiResponse();
        setField(response, "events", wrapper);

        given(newsFetchService.fetchEvents(any(LocalDate.class), eq(1)))
                .willReturn(response);

        ItemReader<EventApiItem> reader = config.newsReader();

        // when
        EventApiItem result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("event-1");
    }

    @Test
    @DisplayName("Processor는 EventApiItem의 infoArticle uri로 본문 기사를 재조회하고 News와 Keyword를 생성한다")
    void newsProcessor() throws Exception {
        // given
        EventApiItem eventItem = createMockEvent("event-uri-1", "article-uri-1");
        NewsApiItem fullArticle = createMockArticle("article-uri-1");

        Keyword keyword = Keyword.create("테슬라");

        given(newsFetchService.fetchArticle("article-uri-1"))
                .willReturn(fullArticle);

        given(keywordRepository.findByNormalizedWord("테슬라"))
                .willReturn(Optional.empty());

        given(keywordRepository.findByAlias("테슬라"))
                .willReturn(Optional.empty());

        given(keywordRepository.save(any(Keyword.class)))
                .willReturn(keyword);

        ItemProcessor<EventApiItem, News> processor = config.newsProcessor();

        // when
        News result = processor.process(eventItem);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("article-uri-1");
        assertThat(result.getTitle()).isEqualTo("Full Article Title");
        assertThat(result.getBody()).contains("full article body");
        assertThat(result.getUrl()).isEqualTo("https://news.com/article-uri-1");
        assertThat(result.getLang()).isEqualTo("kor");
        assertThat(result.getNewsKeywords()).isNotEmpty();

        verify(newsFetchService).fetchArticle("article-uri-1");
        verify(keywordRepository).save(any(Keyword.class));
    }

    @Test
    @DisplayName("Writer는 이미 존재하는 URI를 제외하고 새로운 뉴스만 저장한다")
    void newsWriter() throws Exception {
        // given
        ItemWriter<News> writer = config.newsWriter();

        LocalDateTime now = LocalDateTime.now();
        News n1 = News.create("uri1", "t1", "b1", "https://news.com/1", "kor", 0.1, now);
        News n2 = News.create("uri2", "t2", "b2", "https://news.com/2", "kor", 0.2, now);

        given(newsRepository.findExistingUris(anyList()))
                .willReturn(List.of("uri1"));

        Chunk<News> chunk = new Chunk<>(List.of(n1, n2));

        // when
        writer.write(chunk);

        // then
        verify(newsRepository).saveAll(argThat(list -> {
            List<News> savedList = new ArrayList<>();
            list.forEach(savedList::add);

            return savedList.size() == 1
                    && savedList.get(0).getUri().equals("uri2");
        }));
    }

    private EventApiItem createMockEvent(String eventUri, String articleUri) {
        EventApiItem event = new EventApiItem();
        setField(event, "uri", eventUri);
        setField(event, "sentiment", 0.5);

        EventTitle title = new EventTitle();
        setField(title, "kor", "테스트 이벤트 제목");
        setField(title, "eng", "Test Event Title");
        setField(event, "title", title);

        Concept concept = new Concept();
        setField(concept, "type", "org");
        setField(concept, "score", 100);

        ConceptLabel label = new ConceptLabel();
        setField(label, "kor", "테슬라");
        setField(label, "eng", "Tesla");
        setField(concept, "label", label);

        setField(event, "concepts", List.of(concept));

        NewsApiItem infoArticle = new NewsApiItem();
        setField(infoArticle, "uri", articleUri);
        setField(infoArticle, "url", "https://news.com/" + articleUri);
        setField(infoArticle, "lang", "kor");

        InfoArticleWrapper articleWrapper = new InfoArticleWrapper();
        setField(articleWrapper, "kor", infoArticle);
        setField(articleWrapper, "eng", infoArticle);

        setField(event, "infoArticle", articleWrapper);

        return event;
    }

    private NewsApiItem createMockArticle(String articleUri) {
        NewsApiItem article = new NewsApiItem();

        setField(article, "uri", articleUri);
        setField(article, "url", "https://news.com/" + articleUri);
        setField(article, "lang", "kor");
        setField(article, "dateTimePub", "2026-05-01T10:00:00Z");
        setField(article, "title", "Full Article Title");
        setField(article, "body", "This is the full article body content. This body should be saved into news table.");

        return article;
    }
}