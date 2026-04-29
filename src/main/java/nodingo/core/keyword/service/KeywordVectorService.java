package nodingo.core.keyword.service;

import lombok.RequiredArgsConstructor;
import nodingo.core.keyword.domain.Keyword;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordVectorService {
    private final EmbeddingModel embeddingModel;

    public void updateKeywordEmbedding(Keyword keyword) {
        float[] vector = embeddingModel.embed(keyword.getWord());
        keyword.updateEmbedding(vector);
    }
}
