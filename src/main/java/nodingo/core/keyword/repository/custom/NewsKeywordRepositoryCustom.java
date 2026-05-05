package nodingo.core.keyword.repository.custom;

import nodingo.core.keyword.domain.NewsKeyword;
import java.util.List;

public interface NewsKeywordRepositoryCustom {
    List<NewsKeyword> findTopByKeywordId(Long keywordId, int limit);
}
