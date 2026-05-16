package nodingo.core.batch.service.query;

import lombok.RequiredArgsConstructor;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class NewsBatchQueryService {
    private final NewsRepository newsRepository;

    public NewsBatch.Request getRealNewsRequestJson() {
        List<News> realNewsList = newsRepository.findAll(PageRequest.of(0, 100)).getContent();

        List<NewsBatch.NewsInput> newsInputs = realNewsList.stream()
                .map(n -> NewsBatch.NewsInput.builder()
                        .newsId(n.getId())
                        .title(n.getTitle())
                        .body(n.getBody())
                        .build())
                .toList();

        List<NewsBatch.ExistingKeywordInput> existingKeywords = new ArrayList<>();

        return NewsBatch.Request.builder()
                .news(newsInputs)
                .existingKeywords(existingKeywords)
                .topKKeywords(5)
                .build();
    }
}
