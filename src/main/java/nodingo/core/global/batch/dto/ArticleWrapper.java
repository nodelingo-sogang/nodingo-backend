package nodingo.core.global.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class ArticleWrapper {
    private List<NewsApiItem> results;
    private int totalResults;
    private int page;
    private int count;
}
