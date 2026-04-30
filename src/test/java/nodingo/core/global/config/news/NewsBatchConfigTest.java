package nodingo.core.global.config.news;

import nodingo.core.batch.dto.ArticleWrapper;
import nodingo.core.batch.dto.NewsApiItem;
import nodingo.core.batch.dto.NewsApiResponse;
import nodingo.core.batch.service.NewsFetchService;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.Chunk;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

class NewsBatchConfigTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsFetchService newsFetchService;

    private NewsBatchConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new NewsBatchConfig(
                null,
                null,
                newsRepository,
                newsFetchService,
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
    void newsReader() throws Exception {
        // given
        NewsApiItem item = new NewsApiItem();
        setField(item, "uri", "test-uri");
        setField(item, "title", "title");
        setField(item, "body", "body");
        setField(item, "url", "url");
        setField(item, "lang", "en");
        setField(item, "sentiment", 0.5);
        setField(item, "dateTimePub", "2024-01-01T10:00:00");

        ArticleWrapper wrapper = new ArticleWrapper();
        setField(wrapper, "results", List.of(item));
        setField(wrapper, "pages", 1);

        NewsApiResponse response = new NewsApiResponse();
        setField(response, "articles", wrapper);

        given(newsFetchService.fetchNews(any(LocalDate.class), eq(1), isNull()))
                .willReturn(response);

        ItemReader<NewsApiItem> reader = config.newsReader();

        // when
        NewsApiItem result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("test-uri");
    }

    @Test
    void newsWriter() throws Exception {
        // given
        ItemWriter<News> writer = config.newsWriter();

        // News.create() 사용 (setter 없음)
        News n1 = News.create("uri1", "t1", "b1", "u1", "en", 0.1, null);
        News n2 = News.create("uri2", "t2", "b2", "u2", "en", 0.2, null);

        given(newsRepository.findExistingUris(any()))
                .willReturn(List.of("uri1"));

        Chunk<News> chunk = new Chunk<>(List.of(n1, n2));

        // when
        writer.write(chunk);

        // then
        verify(newsRepository).saveAll(argThat(iterable -> {
            List<News> list = new ArrayList<>();
            iterable.forEach(list::add);

            assertThat(list)
                    .hasSize(1)
                    .extracting(News::getUri)
                    .containsExactly("uri2");

            return true;
        }));
    }
}