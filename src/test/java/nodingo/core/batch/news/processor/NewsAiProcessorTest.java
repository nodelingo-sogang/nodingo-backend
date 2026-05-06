package nodingo.core.batch.news.processor;
import nodingo.core.batch.dto.article.NewsApiItem;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NewsAiProcessorTest {

    @Mock private NewsRepository newsRepository;
    @InjectMocks private NewsAiProcessor processor;

    @Test
    @DisplayName("중복되지 않은 신규 뉴스는 엔티티로 변환되어야 한다")
    void process_Success() throws Exception {
        // given
        NewsApiItem item = new NewsApiItem();
        ReflectionTestUtils.setField(item, "uri", "news-123");
        ReflectionTestUtils.setField(item, "title", "테스트 제목");
        ReflectionTestUtils.setField(item, "body", "충분히 긴 본문 내용입니다...");
        ReflectionTestUtils.setField(item, "url", "https://test.com");
        ReflectionTestUtils.setField(item, "lang", "kor");
        ReflectionTestUtils.setField(item, "dateTimePub", "2026-05-06T10:00:00Z");

        given(newsRepository.existsByUri("news-123")).willReturn(false);

        // when
        News result = processor.process(item);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("news-123");
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("이미 존재하는 URI의 뉴스는 null을 반환하여 필터링한다")
    void process_SkipDuplicate() throws Exception {
        // given
        NewsApiItem item = new NewsApiItem();
        ReflectionTestUtils.setField(item, "uri", "existing-uri");
        given(newsRepository.existsByUri("existing-uri")).willReturn(true);

        // when
        News result = processor.process(item);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("본문(Body)이 비어있는 뉴스는 null을 반환하여 필터링한다")
    void process_SkipEmptyBody() throws Exception {
        // given
        NewsApiItem item = new NewsApiItem();
        ReflectionTestUtils.setField(item, "uri", "news-1");
        ReflectionTestUtils.setField(item, "body", "");

        // when
        News result = processor.process(item);

        // then
        assertThat(result).isNull();
    }
}