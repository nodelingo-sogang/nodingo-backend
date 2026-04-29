package nodingo.core.news.service;

import lombok.RequiredArgsConstructor;
import nodingo.core.news.domain.News;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsVectorService {
    private final EmbeddingModel embeddingModel;

    public void updateNewsEmbedding(News news) {
        String content = news.getTitle() + " " + news.getBody();
        float[] vector = embeddingModel.embed(content);
        news.updateEmbedding(vector);
    }
}
