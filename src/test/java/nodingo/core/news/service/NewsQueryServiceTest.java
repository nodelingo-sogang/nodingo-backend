package nodingo.core.news.service;

import nodingo.core.global.exception.news.NewsNotFoundException;
import nodingo.core.news.domain.News;
import nodingo.core.news.dto.result.NewsDetailResult;
import nodingo.core.news.repository.NewsRepository;
import nodingo.core.news.service.query.NewsQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class NewsQueryServiceTest {

    @InjectMocks
    private NewsQueryService newsQueryService;

    @Mock
    private NewsRepository newsRepository;

    @Test
    @DisplayName("뉴스 상세 조회 성공 - fetch join 메서드를 호출하고 Result로 잘 변환되어야 함")
    void getNewsDetail_Success() {
        // 1. Given
        Long newsId = 1L;
        LocalDateTime now = LocalDateTime.now();

        News news = News.create(
                "uri-123",
                "삼성전자 HBM4 공개",
                "본문 내용입니다.",
                "https://news.com/123",
                "kor",
                0.8,
                now
        );
        ReflectionTestUtils.setField(news, "id", newsId);

        given(newsRepository.findByIdWithKeywords(newsId)).willReturn(Optional.of(news));

        // 2. When
        NewsDetailResult result = newsQueryService.getNewsDetail(newsId);

        // 3. Then
        assertThat(result.getId()).isEqualTo(newsId);
        assertThat(result.getTitle()).isEqualTo("삼성전자 HBM4 공개");
        assertThat(result.getBody()).isEqualTo("본문 내용입니다.");
        assertThat(result.getUrl()).isEqualTo("https://news.com/123");
        assertThat(result.getDateTimePub()).isEqualTo(now);

        verify(newsRepository, times(1)).findByIdWithKeywords(newsId);
    }

    @Test
    @DisplayName("존재하지 않는 뉴스 ID 조회 시 NewsNotFoundException 발생")
    void getNewsDetail_NotFound() {
        // Given
        Long newsId = 999L;
        given(newsRepository.findByIdWithKeywords(anyLong())).willReturn(Optional.empty());

        // When & Then
        assertThrows(NewsNotFoundException.class, () -> {
            newsQueryService.getNewsDetail(newsId);
        });
    }
}