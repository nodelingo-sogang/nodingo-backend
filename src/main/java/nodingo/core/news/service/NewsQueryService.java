package nodingo.core.news.service;

import lombok.RequiredArgsConstructor;
import nodingo.core.global.exception.news.NewsNotFoundException;
import nodingo.core.news.domain.News;
import nodingo.core.news.dto.result.NewsDetailResult;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsQueryService {

    private final NewsRepository newsRepository;

    public NewsDetailResult getNewsDetail(Long newsId) {
        News news = getNewsElseThrow(newsId);
        return NewsDetailResult.from(news);
    }


    private News getNewsElseThrow(Long newsId) {
        return newsRepository.findByIdWithKeywords(newsId)
                .orElseThrow(() -> new NewsNotFoundException("해당 뉴스를 찾을 수 없습니다"));
    }
}
