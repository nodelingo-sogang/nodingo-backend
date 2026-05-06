package nodingo.core.batch.news.reader;
import nodingo.core.batch.dto.article.ArticleWrapper;
import nodingo.core.batch.dto.article.NewsApiItem;
import nodingo.core.batch.dto.article.NewsApiResponse;
import nodingo.core.batch.service.query.NewsFetchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class NewsApiReaderTest {

    @Mock private NewsFetchService newsFetchService;
    @InjectMocks private NewsApiReader reader;

    @Test
    @DisplayName("API로부터 받은 뉴스 목록을 Iterator를 통해 순차적으로 읽어야 한다")
    void read_Success() throws Exception {
        // given
        NewsApiItem item = new NewsApiItem();
        ReflectionTestUtils.setField(item, "uri", "news-1");

        NewsApiResponse response = new NewsApiResponse();
        ArticleWrapper wrapper = new ArticleWrapper();

        ReflectionTestUtils.setField(wrapper, "results", List.of(item));
        ReflectionTestUtils.setField(wrapper, "pages", 1);

        ReflectionTestUtils.setField(response, "articles", wrapper);

        given(newsFetchService.fetchNews(any(LocalDate.class), eq(1)))
                .willReturn(response);

        // when
        NewsApiItem result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("news-1");
    }
}