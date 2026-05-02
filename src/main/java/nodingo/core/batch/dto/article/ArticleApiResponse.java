package nodingo.core.batch.dto.article;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.batch.dto.event.NewsApiItem;


@Getter
@NoArgsConstructor
public class ArticleApiResponse {

    private ArticleWrapper articles;

    public NewsApiItem getFirstArticle() {
        if (articles == null || articles.getResults() == null || articles.getResults().isEmpty()) {
            return null;
        }

        return articles.getResults().get(0);
    }
}
