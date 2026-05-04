package nodingo.core.batch.news.processor;

import nodingo.core.batch.dto.event.*;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsAiProcessorTest {

    @Mock
    private NewsRepository newsRepository;
    @Mock private KeywordRepository keywordRepository;
    @Mock private NewsFetchService newsFetchService;
    @InjectMocks
    private NewsAiProcessor processor;

    @Test
    @DisplayName("신규 뉴스를 가공하여 엔티티로 변환하고 키워드를 연결한다")
    void process_Success() throws Exception {
        // given
        EventApiItem eventItem = createMockEvent("event-uri", "article-uri");
        NewsApiItem fullArticle = createMockArticle("article-uri");
        Keyword keyword = Keyword.create("테슬라");

        given(newsRepository.existsByUri("article-uri")).willReturn(false);
        given(newsFetchService.fetchArticle("article-uri")).willReturn(fullArticle);
        given(keywordRepository.findByNormalizedWord("테슬라")).willReturn(Optional.empty());
        given(keywordRepository.findByAlias("테슬라")).willReturn(Optional.empty());
        given(keywordRepository.save(any(Keyword.class))).willReturn(keyword);

        // when
        News result = processor.process(eventItem);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("article-uri");
        assertThat(result.getNewsKeywords()).isNotEmpty();
        verify(newsFetchService).fetchArticle("article-uri");
    }

    // --- Helper Methods (기존 코드 복붙) ---
    private EventApiItem createMockEvent(String eventUri, String articleUri) {
        EventApiItem event = new EventApiItem();
        setField(event, "uri", eventUri);

        EventTitle title = new EventTitle();
        setField(title, "kor", "테스트 제목");
        setField(event, "title", title);

        Concept concept = new Concept();
        setField(concept, "score", 100);
        ConceptLabel label = new ConceptLabel();
        setField(label, "kor", "테슬라");
        setField(concept, "label", label);
        setField(event, "concepts", List.of(concept));

        NewsApiItem infoArticle = new NewsApiItem();
        setField(infoArticle, "uri", articleUri);
        InfoArticleWrapper articleWrapper = new InfoArticleWrapper();
        setField(articleWrapper, "kor", infoArticle);
        setField(event, "infoArticle", articleWrapper);

        return event;
    }

    private NewsApiItem createMockArticle(String articleUri) {
        NewsApiItem article = new NewsApiItem();
        setField(article, "uri", articleUri);
        setField(article, "title", "기사 제목");
        setField(article, "body", "기사 본문 내용입니다.");
        return article;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}