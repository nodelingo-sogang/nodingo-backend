package nodingo.core.keyword.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KeywordCandidate {
    private Long keywordId;
    private String word;
    private float[] embedding;

    @QueryProjection
    public KeywordCandidate(Long keywordId, String word, float[] embedding) {
        this.keywordId = keywordId;
        this.word = word;
        this.embedding = embedding;
    }
}