package nodingo.core.news.repository.custom;

import nodingo.core.news.domain.News;
import nodingo.core.news.dto.query.NewsResult;
import nodingo.core.user.domain.UserScrap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepositoryCustom {
    Slice<NewsResult> findRecentNewsByKeywords(List<String> keywordNames, LocalDateTime limitTime, Pageable pageable);
    Slice<NewsResult> findNewsByLanguage(String language, LocalDateTime limitTime, Pageable pageable);
    List<News> findTop5SimilarNews(Long newsId, int limit);
    Optional<UserScrap> findScrapDetail(Long userId, Long newsId);
}
