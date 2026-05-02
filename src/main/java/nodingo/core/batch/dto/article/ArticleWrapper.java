package nodingo.core.batch.dto.article;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.batch.dto.event.NewsApiItem;

import java.util.List;

@Getter
@NoArgsConstructor
public class ArticleWrapper {

    private List<NewsApiItem> results;
    private Integer totalResults;
    private Integer page;
    private Integer count;
    private Integer pages;
}