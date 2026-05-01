package nodingo.core.user.service.vector;

import lombok.RequiredArgsConstructor;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.news.domain.News;
import nodingo.core.user.domain.User;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserVectorService {

    private final EmbeddingModel embeddingModel;

    /**
     * 1. 온보딩 완료 시 초기 유저 임베딩 생성
     * 선택한 키워드들의 텍스트를 합쳐서 유저의 '태초 좌표'를 설정함
     */
    public void initUserEmbedding(User user, List<Keyword> selectedKeywords) {
        if (selectedKeywords == null || selectedKeywords.isEmpty()) {
            return;
        }

        String combinedKeywords = selectedKeywords.stream()
                .map(Keyword::getWord)
                .collect(Collectors.joining(", "));
        float[] initialVector = embeddingModel.embed(combinedKeywords);
        user.updateEmbedding(initialVector);
    }

    /**
     * 2. 뉴스 스크랩 시 유저 임베딩 업데이트
     * 스크랩한 뉴스의 벡터를 유저 벡터에 섞어서 유저의 관심사 좌표를 이동시킴
     */
    public void updateUserVectorByScrap(User user, News scrappedNews) {
        float[] currentUserVector = user.getEmbedding();
        float[] newsVector = scrappedNews.getEmbedding();

        if (currentUserVector == null) {
            user.updateEmbedding(newsVector);
            return;
        }

        float learningRate = 0.3f;
        float[] updatedVector = calculateMovingAverage(currentUserVector, newsVector, learningRate);

        user.updateEmbedding(updatedVector);
    }

    private float[] calculateMovingAverage(float[] current, float[] target, float weight) {
        if (current.length != target.length) {
            throw new IllegalArgumentException("벡터의 차원이 일치하지 않습니다.");
        }

        float[] result = new float[current.length];
        for (int i = 0; i < current.length; i++) {
            result[i] = (current[i] * (1.0f - weight)) + (target[i] * weight);
        }

        return normalize(result);
    }

    private float[] normalize(float[] vector) {
        float sum = 0;
        for (float v : vector) sum += v * v;
        float norm = (float) Math.sqrt(sum);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) vector[i] /= norm;
        }
        return vector;
    }
}
