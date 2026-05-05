package nodingo.core.batch.recommend.processor;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.keyword.KeywordSummary;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.domain.NewsKeyword;
import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.NewsKeywordRepository;
import nodingo.core.news.domain.News;
import nodingo.core.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;


import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendSummaryProcessorTest {

    @InjectMocks
    private RecommendSummaryProcessor processorConfig;

    @Mock private NewsKeywordRepository newsKeywordRepository;
    @Mock private AiClient aiClient;

    private ItemProcessor<RecommendKeyword, RecommendKeyword> processor;

    @BeforeEach
    void setUp() {
        processor = processorConfig.recommendSummaryItemProcessor();
    }

    @Test
    @DisplayName("이미 summary가 있으면 skip")
    void skipIfSummaryExists() throws Exception {
        // given
        RecommendKeyword rk = mock(RecommendKeyword.class);
        given(rk.getSummary()).willReturn("exists");

        // when
        RecommendKeyword result = processor.process(rk);

        // then
        assertThat(result).isNull();
        verifyNoInteractions(aiClient);
    }

    @Test
    @DisplayName("뉴스 없으면 fallback summary")
    void fallbackWhenNoNews() throws Exception {
        // given
        User user = mock(User.class);
        Keyword keyword = mock(Keyword.class);

        RecommendKeyword rk = RecommendKeyword.create(user, keyword, LocalDate.now(), 0.9);

        given(keyword.getId()).willReturn(1L);
        given(newsKeywordRepository.findTopByKeywordId(1L, 3))
                .willReturn(List.of());

        // when
        RecommendKeyword result = processor.process(rk);

        // then
        assertThat(result.getSummary()).isEqualTo("관련 뉴스가 부족하여 요약할 수 없습니다.");
    }

    @Test
    @DisplayName("정상: AI 호출 후 summary 저장")
    void success() throws Exception {
        // given
        User user = mock(User.class);
        Keyword keyword = mock(Keyword.class);
        NewsKeyword nk = mock(NewsKeyword.class);
        News news = mock(News.class);

        RecommendKeyword rk = RecommendKeyword.create(user, keyword, LocalDate.now(), 0.9);

        given(user.getId()).willReturn(10L);
        given(keyword.getId()).willReturn(1L);
        given(keyword.getWord()).willReturn("AI");

        given(nk.getNews()).willReturn(news);
        given(news.getId()).willReturn(100L);
        given(news.getTitle()).willReturn("title");
        given(news.getBody()).willReturn("body");

        given(newsKeywordRepository.findTopByKeywordId(1L, 3))
                .willReturn(List.of(nk));

        KeywordSummary.Response response = KeywordSummary.Response.builder()
                .summary("AI summary")
                .build();

        given(aiClient.summarizeKeywords(any())).willReturn(response);

        // when
        RecommendKeyword result = processor.process(rk);

        // then
        verify(aiClient).summarizeKeywords(any());
        assertThat(result.getSummary()).isEqualTo("AI summary");
    }
}