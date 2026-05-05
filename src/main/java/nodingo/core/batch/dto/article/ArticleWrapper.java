package nodingo.core.batch.dto.article;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ArticleWrapper {
    private List<NewsApiItem> results;
    private int totalResults;
    private int pages;
    private int count;
}