package nodingo.core.global.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NewsApiResponse {
    private ArticleWrapper articles;
}
