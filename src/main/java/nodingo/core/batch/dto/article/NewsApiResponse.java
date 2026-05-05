package nodingo.core.batch.dto.article;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NewsApiResponse {
    private ArticleWrapper articles;
}