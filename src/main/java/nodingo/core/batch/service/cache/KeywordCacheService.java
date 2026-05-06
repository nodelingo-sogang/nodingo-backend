package nodingo.core.batch.service.cache;

import lombok.RequiredArgsConstructor;
import nodingo.core.ai.dto.newsBatch.NewsBatch;
import nodingo.core.keyword.repository.KeywordRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordCacheService {
    private final KeywordRepository keywordRepository;

    @Cacheable(value = "batch:keyword", key = "'all'")
    public List<NewsBatch.ExistingKeywordInput> getAllKeywords() {
        return keywordRepository.findAll().stream()
                .map(k -> NewsBatch.ExistingKeywordInput.builder()
                        .keywordId(k.getId())
                        .word(k.getWord())
                        .normalizedWord(k.getNormalizedWord())
                        .embedding(k.getEmbedding())
                        .build())
                .toList();
    }
}
